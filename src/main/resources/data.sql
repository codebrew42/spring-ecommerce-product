-- Development seed data
-- Keep minimal for production deployment
INSERT INTO products (name, price, imageUrl) VALUES
('Laptop', 1500.0, 'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=400&h=400&fit=crop'),
('Phone', 800.0, 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=400&h=400&fit=crop');

-- Members should be created via registration endpoint
-- No seed member data in production