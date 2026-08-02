package com.example.foodpickupapp.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.foodpickupapp.database.DatabaseContract.OrderEntry;
import com.example.foodpickupapp.database.DatabaseContract.OrderItemEntry;
import com.example.foodpickupapp.database.FoodPickupDbHelper;
import com.example.foodpickupapp.model.Order;
import com.example.foodpickupapp.model.OrderItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the Orders and OrderItems tables.
 * Provides operations to save and retrieve completed order details.
 *
 * Related to: FOOD-17 (save completed order details to the database)
 */
public class OrderDao {

    private final FoodPickupDbHelper dbHelper;

    public OrderDao(FoodPickupDbHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    /**
     * Inserts a new order along with its line items in a single transaction.
     * This ensures that either the entire order is saved, or nothing is.
     *
     * @param order the order header
     * @param items the list of items in the order
     * @return the row ID of the newly inserted order, or -1 if an error occurred
     */
    public long insertOrder(Order order, List<OrderItem> items) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long orderId = -1;

        db.beginTransaction();
        try {
            // Insert the order header
            ContentValues orderValues = new ContentValues();
            orderValues.put(OrderEntry.COLUMN_USER_ID, order.getUserId());
            orderValues.put(OrderEntry.COLUMN_RESTAURANT_ID, order.getRestaurantId());
            orderValues.put(OrderEntry.COLUMN_TOTAL_AMOUNT, order.getTotalAmount());
            orderValues.put(OrderEntry.COLUMN_STATUS, order.getStatus());

            if (order.getPaymentReference() != null) {
                orderValues.put(OrderEntry.COLUMN_PAYMENT_REFERENCE, order.getPaymentReference());
            }

            orderId = db.insert(OrderEntry.TABLE_NAME, null, orderValues);

            if (orderId == -1) {
                return -1; // Order insert failed
            }

            // Insert each order item linked to this order
            for (OrderItem item : items) {
                ContentValues itemValues = new ContentValues();
                itemValues.put(OrderItemEntry.COLUMN_ORDER_ID, orderId);
                itemValues.put(OrderItemEntry.COLUMN_FOOD_ITEM_ID, item.getFoodItemId());
                itemValues.put(OrderItemEntry.COLUMN_QUANTITY, item.getQuantity());
                itemValues.put(OrderItemEntry.COLUMN_PRICE_AT_PURCHASE, item.getPriceAtPurchase());

                long itemId = db.insert(OrderItemEntry.TABLE_NAME, null, itemValues);
                if (itemId == -1) {
                    return -1; // Item insert failed, transaction will be rolled back
                }
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        return orderId;
    }

    /**
     * Retrieves an order by its ID.
     *
     * @param orderId the order ID
     * @return the Order if found, or null
     */
    public Order getOrderById(long orderId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = OrderEntry._ID + " = ?";
        String[] selectionArgs = { String.valueOf(orderId) };

        Cursor cursor = db.query(
                OrderEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null, null, null
        );

        Order order = null;
        if (cursor.moveToFirst()) {
            order = cursorToOrder(cursor);
        }
        cursor.close();
        return order;
    }

    /**
     * Retrieves all order items for a specific order.
     *
     * @param orderId the order ID
     * @return a list of OrderItem objects
     */
    public List<OrderItem> getOrderItems(long orderId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = OrderItemEntry.COLUMN_ORDER_ID + " = ?";
        String[] selectionArgs = { String.valueOf(orderId) };

        Cursor cursor = db.query(
                OrderItemEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null, null, null
        );

        List<OrderItem> items = new ArrayList<>();
        while (cursor.moveToNext()) {
            items.add(cursorToOrderItem(cursor));
        }
        cursor.close();
        return items;
    }

    /**
     * Retrieves all orders placed by a specific user.
     * Ordered by creation date, most recent first.
     *
     * @param userId the user ID
     * @return a list of orders
     */
    public List<Order> getOrdersByUser(long userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = OrderEntry.COLUMN_USER_ID + " = ?";
        String[] selectionArgs = { String.valueOf(userId) };
        String orderBy = OrderEntry.COLUMN_CREATED_AT + " DESC";

        Cursor cursor = db.query(
                OrderEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null, null,
                orderBy
        );

        List<Order> orders = new ArrayList<>();
        while (cursor.moveToNext()) {
            orders.add(cursorToOrder(cursor));
        }
        cursor.close();
        return orders;
    }

    /**
     * Retrieves all orders for a specific restaurant.
     * Ordered by creation date, most recent first.
     *
     * @param restaurantId the restaurant ID
     * @return a list of orders for that restaurant
     */
    public List<Order> getOrdersByRestaurant(long restaurantId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = OrderEntry.COLUMN_RESTAURANT_ID + " = ?";
        String[] selectionArgs = { String.valueOf(restaurantId) };
        String orderBy = OrderEntry.COLUMN_CREATED_AT + " DESC";

        Cursor cursor = db.query(
                OrderEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null, null,
                orderBy
        );

        List<Order> orders = new ArrayList<>();
        while (cursor.moveToNext()) {
            orders.add(cursorToOrder(cursor));
        }
        cursor.close();
        return orders;
    }

    /**
     * Retrieves all active orders (PAID or PREPARING).
     * Ordered by creation date, oldest first, so staff prepares them in order.
     *
     * @param restaurantId the restaurant ID to filter by, or -1 for all restaurants
     * @return a list of active orders
     */
    public List<Order> getActiveOrders(long restaurantId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection;
        String[] selectionArgs;

        if (restaurantId > 0) {
            selection = "(" + OrderEntry.COLUMN_STATUS + " = ? OR " + OrderEntry.COLUMN_STATUS + " = ?) AND " + OrderEntry.COLUMN_RESTAURANT_ID + " = ?";
            selectionArgs = new String[]{ "PAID", "PREPARING", String.valueOf(restaurantId) };
        } else {
            selection = OrderEntry.COLUMN_STATUS + " = ? OR " + OrderEntry.COLUMN_STATUS + " = ?";
            selectionArgs = new String[]{ "PAID", "PREPARING" };
        }

        String orderBy = OrderEntry.COLUMN_CREATED_AT + " ASC";

        Cursor cursor = db.query(
                OrderEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null, null,
                orderBy
        );

        List<Order> orders = new ArrayList<>();
        while (cursor.moveToNext()) {
            orders.add(cursorToOrder(cursor));
        }
        cursor.close();
        return orders;
    }

    /**
     * Updates the status of an order.
     * Valid statuses: PLACED, PAID, PREPARING, READY, PICKED_UP
     *
     * @param orderId   the order ID
     * @param newStatus the new status
     * @return the number of rows affected
     *
     * TODO: Will be used by kitchen dashboard in Sprint 8 (FOOD-19, FOOD-20)
     */
    public int updateOrderStatus(long orderId, String newStatus) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(OrderEntry.COLUMN_STATUS, newStatus);
        values.put(OrderEntry.COLUMN_UPDATED_AT, "datetime('now')");

        String selection = OrderEntry._ID + " = ?";
        String[] selectionArgs = { String.valueOf(orderId) };

        return db.update(OrderEntry.TABLE_NAME, values, selection, selectionArgs);
    }

    /**
     * Updates the status and payment reference of an order after payment.
     * Used by PaymentActivity to record the transaction reference from
     * the payment gateway.
     *
     * @param orderId          the order ID
     * @param newStatus        the new status (typically "PAID")
     * @param paymentReference the transaction reference from the payment gateway
     * @return the number of rows affected
     *
     * Related to: FOOD-15 (payment API integration)
     */
    public int updateOrderPayment(long orderId, String newStatus, String paymentReference) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(OrderEntry.COLUMN_STATUS, newStatus);
        values.put(OrderEntry.COLUMN_PAYMENT_REFERENCE, paymentReference);

        String selection = OrderEntry._ID + " = ?";
        String[] selectionArgs = { String.valueOf(orderId) };

        return db.update(OrderEntry.TABLE_NAME, values, selection, selectionArgs);
    }

    /**
     * Helper method to convert a database cursor row into an Order object.
     */
    private Order cursorToOrder(Cursor cursor) {
        Order order = new Order();
        order.setId(cursor.getLong(cursor.getColumnIndexOrThrow(OrderEntry._ID)));
        order.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow(OrderEntry.COLUMN_USER_ID)));
        order.setRestaurantId(cursor.getLong(cursor.getColumnIndexOrThrow(OrderEntry.COLUMN_RESTAURANT_ID)));
        order.setTotalAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(OrderEntry.COLUMN_TOTAL_AMOUNT)));
        order.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(OrderEntry.COLUMN_STATUS)));

        int paymentRefIndex = cursor.getColumnIndexOrThrow(OrderEntry.COLUMN_PAYMENT_REFERENCE);
        if (!cursor.isNull(paymentRefIndex)) {
            order.setPaymentReference(cursor.getString(paymentRefIndex));
        }

        order.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(OrderEntry.COLUMN_CREATED_AT)));
        order.setUpdatedAt(cursor.getString(cursor.getColumnIndexOrThrow(OrderEntry.COLUMN_UPDATED_AT)));
        return order;
    }

    /**
     * Helper method to convert a database cursor row into an OrderItem object.
     */
    private OrderItem cursorToOrderItem(Cursor cursor) {
        OrderItem item = new OrderItem();
        item.setId(cursor.getLong(cursor.getColumnIndexOrThrow(OrderItemEntry._ID)));
        item.setOrderId(cursor.getLong(cursor.getColumnIndexOrThrow(OrderItemEntry.COLUMN_ORDER_ID)));
        item.setFoodItemId(cursor.getLong(cursor.getColumnIndexOrThrow(OrderItemEntry.COLUMN_FOOD_ITEM_ID)));
        item.setQuantity(cursor.getInt(cursor.getColumnIndexOrThrow(OrderItemEntry.COLUMN_QUANTITY)));
        item.setPriceAtPurchase(cursor.getDouble(cursor.getColumnIndexOrThrow(OrderItemEntry.COLUMN_PRICE_AT_PURCHASE)));
        return item;
    }
}
