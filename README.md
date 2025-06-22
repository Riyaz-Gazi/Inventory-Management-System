# 📦 Inventory Service

This microservice manages inventory data including total and reserved quantities of items in an e-commerce system.

---

## 🗄️ Database Schema

The `inventory` table stores details for each item including quantities and versioning for concurrency control.

### ✅ Table: `InventoryItem`

```sql
CREATE TABLE inventory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_id BIGINT UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    total_quantity INT NOT NULL,
    reserved_quantity INT NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);
```

| Column              | Type    | Constraints                 | Description                           |
| ------------------- | ------- | --------------------------- | ------------------------------------- |
| `id`                | BIGINT  | Primary Key, Auto-increment | Internal unique identifier            |
| `item_id`           | BIGINT  | Unique, Not Null            | Business identifier for the item      |
| `name`              | VARCHAR | Not Null                    | Name of the item                      |
| `total_quantity`    | INT     | Not Null                    | Total quantity available in inventory |
| `reserved_quantity` | INT     | Not Null                    | Quantity currently reserved           |
| `version`           | BIGINT  | Not Null, Default 0         | Used for optimistic locking           |


### ✅ Table: `Reservation`

```sql
CREATE TABLE reservation (
id BIGINT AUTO_INCREMENT PRIMARY KEY,
item_id BIGINT NOT NULL,
quantity INT NOT NULL,
reservation_status VARCHAR(20) NOT NULL,
created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
reserved_by VARCHAR(255) NOT NULL,
reservation_token VARCHAR(255) NOT NULL UNIQUE
);
```

## 🗃️ Reservation Table Schema

| Column Name        | Data Type         | Constraints             | Description                                |
|--------------------|-------------------|--------------------------|--------------------------------------------|
| id                 | BIGINT            | PRIMARY KEY, AUTO_INCREMENT | Unique identifier for each reservation     |
| item_id            | BIGINT            | NOT NULL                | ID of the item being reserved              |
| quantity           | INT               | NOT NULL                | Quantity reserved                          |
| reservation_status | VARCHAR           | NOT NULL (Enum)         | Status of the reservation (e.g., PENDING, CONFIRMED, CANCELLED) |
| created_at         | DATETIME / TIMESTAMP | NOT NULL            | Timestamp when reservation was created     |
| reserved_by        | VARCHAR           | NOT NULL                | Identifier for the user or system that made the reservation |
| reservation_token  | VARCHAR           | UNIQUE, NOT NULL        | Unique token used to track the reservation |

```text
com.quarks.ecommerce.inventory_service
├── controller        # Contains REST controllers that handle HTTP requests
├── dto              # Contains Data Transfer Objects used for requests and responses
├── entity           # Contains JPA entity classes mapped to database tables
├── repository       # Contains Spring Data JPA repositories for data access
├── service          # Contains service interfaces and their implementations for business logic
├── exceptions        # Defines custom exceptions used across the application
└── advice           # Handles centralized exception handling using @RestControllerAdvice
```


## 📦 API Endpoints

- **POST** `/inventory` – Create or update item supply
- **POST** `/inventory/reserve` – Reserve item quantity
- **POST** `/inventory/cancel` – Cancel reservation
- **GET** `/inventory/{itemId}/availability` – Check available quantity

## 🔧 Tech Stack

This project is built with modern backend technologies:

| Technology       | Description                       |
|------------------|-----------------------------------|
| **Java 17**      | Primary programming language      |
| **Spring Boot**  | Framework for building RESTful microservices |
| **Spring MVC**   | Handles routing, controller, and HTTP interfaces |
| **JPA (Hibernate)** | ORM tool for database operations |
| **PostgreSQL**   | Relational database      |
| **Lombok**       | Reduces boilerplate code in Java classes |
| **JUnit 5**      | Unit testing framework            |
| **Mockito**      | Mocking framework for testing     |
| **MockMvc**      | Used to test Spring controllers   |
| **Jackson**      | Handles JSON serialization and deserialization | 
 | **Redis**        | Used for store the data in cache| 

# 🚀 Future Implementation Features

The following modules are planned for future development to enhance and complete the e-commerce backend system:

---

## 🧑‍💼 User Management

- User Registration and Login with JWT Authentication
- Role-based Access Control (Admin, Customer)
- Update and Manage User Profile

---

## 🏠 User Address

- Add/Edit/Delete User Addresses
- Mark Default Address
- Support for Multiple Addresses per User

---

## 🛒 Shopping Cart

- Create and Manage Shopping Cart per User
- Guest Cart (Session-based)
- Merge Guest Cart with User Cart on Login

---

## 📦 Cart Item

- Add Items to Cart
- Remove or Update Item Quantity
- Inventory Quantity Validation
- Auto Price Calculation

---

## 📋 Order Management

- Place Order from Shopping Cart
- View Order History
- Track Order Status (Pending, Shipped, Delivered, Cancelled)
- Cancel Order with Inventory Rollback

---

## 📦 Order Item

- Store Each Item Info Within an Order
- Track Individual Item Status (e.g., Delivered, Returned)
- Refunds/Returns for Specific Items

---

## 🎟️ Coupon System

- Apply Discount Coupons
- Support for Fixed/Percentage Discounts
- Enforce Rules: Expiry Date, Usage Limit, Min Order Value

---

## 💳 Billing Information

- Generate Detailed Billing Summary
- Include Taxes and Discounts
- Future Integration: Online Payment Support

---

Stay tuned for more enhancements!

---






