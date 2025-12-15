// Provide types so that test_shapes.ts functions properly as described in comments.

export type RGBColor = { r: number; g: number; b: number };
export type Color = keyof typeof colorTable | RGBColor;

interface BaseShape {
    type: ShapeType;
    color: Color;
}

export interface Circle extends BaseShape {
    radius: number;
}

export interface Rectangle extends BaseShape {
    width: number;
    height: number;
}

export interface Square extends BaseShape {
    width: number;
}

export interface Triangle extends BaseShape {
    base: number;
    height: number;
}


export type ShapeType = 'circle' | 'rectangle' | 'square' | 'triangle';
export type Shape = Circle | Rectangle | Square | Triangle;

export const colorTable: {
    red: RGBColor;
    green: RGBColor;
    blue: RGBColor;
};

export function createCircle(radius: number, color: Color): Circle;
export function createRectangle(width: number, height: number, color: Color): Rectangle;
export function createSquare(width: number, color: Color): Square;
export function createTriangle(base: number, height: number, color: Color): Triangle;

export function createShape(type: 'circle', radius: number, color: Color): Circle;
export function createShape(type: 'rectangle', width: number, height: number, color: Color): Rectangle;
export function createShape(type: 'square', width: number, color: Color): Square;
export function createShape(type: 'triangle', base: number, height: number, color: Color): Triangle;

export function calculateShapeArea(shape: Circle | Rectangle | Triangle): number;

export function invertColor<T extends { color: Color }>(shape: T): T & { color: RGBColor };

// export function filterShapesByType<T extends Shape, K extends T['type']>(
export function filterShapesByType<T extends Shape, K extends ShapeType>(
  shapes: T[],
  types: readonly K[]
): Extract<T, { type: K }>[];

export type ObservableShape<T> = T & {
    on<K extends keyof T>(key: K, callback: (oldValue: T[K], newValue: T[K]) => void): void;
    update(changes: Partial<T>): void;
};

export function makeObservable<T extends { [key: string]: any }>(shape: T): ObservableShape<T>;
