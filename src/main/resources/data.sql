INSERT INTO product (name, price, quantity) VALUES ('새우깡', 1500, 100);
INSERT INTO product (name, price, quantity) VALUES ('홈런볼', 1700, 70);
INSERT INTO product (name, price, quantity) VALUES ('코카콜라 500ml', 2500, 150);
INSERT INTO product (name, price, quantity) VALUES ('제주삼다수 500ml', 950, 200);
INSERT INTO product (name, price, quantity) VALUES ('바나나맛우유', 1700, 110);
INSERT INTO product (name, price, quantity) VALUES ('메로나', 1200, 130);
INSERT INTO product (name, price, quantity) VALUES ('신라면', 1150, 250);
INSERT INTO product (name, price, quantity) VALUES ('삼각김밥 참치마요', 1300, 80);


INSERT INTO purchase (state, purchase_date) VALUES ('COMPLETED', '2025-10-28T10:00:00');
INSERT INTO purchase (state, purchase_date) VALUES ('COMPLETED', '2025-10-29T11:30:00');
INSERT INTO purchase (state, purchase_date) VALUES ('REFUNDED', '2025-10-30T14:15:00');
INSERT INTO purchase (state, purchase_date) VALUES ('COMPLETED', '2025-11-01T12:00:00');
INSERT INTO purchase (state, purchase_date) VALUES ('COMPLETED', '2025-11-02T16:00:00');


INSERT INTO purchase_item (purchase_id, product_id, order_quantity, order_price) VALUES (1, 1, 2, 1500);
INSERT INTO purchase_item (purchase_id, product_id, order_quantity, order_price) VALUES (1, 3, 1, 2500);

INSERT INTO purchase_item (purchase_id, product_id, order_quantity, order_price) VALUES (2, 2, 1, 1700);

INSERT INTO purchase_item (purchase_id, product_id, order_quantity, order_price) VALUES (3, 4, 3, 950);

INSERT INTO purchase_item (purchase_id, product_id, order_quantity, order_price) VALUES (4, 7, 2, 1150);
INSERT INTO purchase_item (purchase_id, product_id, order_quantity, order_price) VALUES (4, 8, 1, 1300);

INSERT INTO purchase_item (purchase_id, product_id, order_quantity, order_price) VALUES (5, 5, 1, 1700);
INSERT INTO purchase_item (purchase_id, product_id, order_quantity, order_price) VALUES (5, 6, 2, 1200);