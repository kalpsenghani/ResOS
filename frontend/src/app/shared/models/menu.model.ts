export interface MenuCategory {
  id: string;
  restaurantId: string;
  name: string;
  description?: string;
  sortOrder: number;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface MenuModifier {
  id?: string;
  name: string;
  priceAdjustment: number;
  required?: boolean;
}

export interface MenuItem {
  id: string;
  categoryId: string;
  name: string;
  description?: string;
  price: number;
  cost?: number;
  imageUrl?: string;
  available: boolean;
  preparationTime?: number;
  allergens: string[];
  sortOrder: number;
  modifiers: MenuModifier[];
  createdAt: string;
  updatedAt: string;
  version: number;
}

export interface CreateCategoryRequest {
  restaurantId: string;
  name: string;
  description?: string;
  sortOrder?: number;
}

export interface UpdateCategoryRequest {
  name?: string;
  description?: string;
  sortOrder?: number;
  active?: boolean;
}

export interface CreateMenuItemRequest {
  categoryId: string;
  name: string;
  description?: string;
  price: number;
  cost?: number;
  imageUrl?: string;
  preparationTime?: number;
  allergens?: string[];
  sortOrder?: number;
  modifiers?: MenuModifier[];
}

export interface UpdateMenuItemRequest {
  categoryId?: string;
  name?: string;
  description?: string;
  price?: number;
  cost?: number;
  imageUrl?: string;
  preparationTime?: number;
  allergens?: string[];
  sortOrder?: number;
  available?: boolean;
  modifiers?: MenuModifier[];
}
