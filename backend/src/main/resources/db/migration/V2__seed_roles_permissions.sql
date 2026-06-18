-- Seed subscription plans
INSERT INTO subscription_plans (name, slug, price_monthly, price_yearly, max_locations, max_staff, features) VALUES
('Starter', 'starter', 29.99, 299.99, 1, 5, '["INVENTORY", "ORDERS", "RESERVATIONS"]'),
('Pro', 'pro', 79.99, 799.99, 3, 25, '["INVENTORY", "ORDERS", "RESERVATIONS", "ANALYTICS", "EMPLOYEES"]'),
('Enterprise', 'enterprise', 199.99, 1999.99, 999, 999, '["INVENTORY", "ORDERS", "RESERVATIONS", "ANALYTICS", "EMPLOYEES", "API_ACCESS"]');

-- Platform role
INSERT INTO roles (name, description, is_system) VALUES
('SUPER_ADMIN', 'Platform administrator with full access', TRUE);

-- Tenant roles (template - created per tenant on registration)
-- Permissions
INSERT INTO permissions (name, module, description) VALUES
('tenant:manage', 'platform', 'Manage tenants'),
('inventory:read', 'inventory', 'View inventory items'),
('inventory:write', 'inventory', 'Create and update inventory items'),
('inventory:delete', 'inventory', 'Delete inventory items'),
('orders:read', 'orders', 'View orders'),
('orders:write', 'orders', 'Create and update orders'),
('orders:delete', 'orders', 'Cancel orders'),
('employees:read', 'employees', 'View employees'),
('employees:write', 'employees', 'Manage employees'),
('reservations:read', 'reservations', 'View reservations'),
('reservations:write', 'reservations', 'Manage reservations'),
('menu:read', 'menu', 'View menu'),
('menu:write', 'menu', 'Manage menu items'),
('analytics:read', 'analytics', 'View analytics dashboards'),
('settings:read', 'settings', 'View tenant settings'),
('settings:write', 'settings', 'Manage tenant settings'),
('users:read', 'users', 'View tenant users'),
('users:write', 'users', 'Manage tenant users');

-- SUPER_ADMIN gets all permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p WHERE r.name = 'SUPER_ADMIN';
