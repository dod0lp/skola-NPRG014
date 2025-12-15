// Provide types so that test_shapes.ts functions properly as described in comments.

export type RGBColor = { r: number; g: number; b: number };
export type Color = (keyof typeof colorTable) | RGBColor;

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


type ShapeMap = {
  circle: Circle;
  rectangle: Rectangle;
  square: Square;
  triangle: Triangle;
};

// Keys are literal strings
export type ShapeType = keyof ShapeMap;
export type Shape = ShapeMap[ShapeType];

export const colorTable: {
    red: RGBColor;
    green: RGBColor;
    blue: RGBColor;
};


export function createCircle(radius: number, color: Color): Circle;
export function createRectangle(width: number, height: number, color: Color): Rectangle;
export function createSquare(width: number, color: Color): Square;
export function createTriangle(base: number, height: number, color: Color): Triangle;

// object type from ShapeMap to double-check spelling of type
export function createShape(type: 'circle', radius: number, color: Color): ShapeMap['circle'];
export function createShape(type: 'rectangle', width: number, height: number, color: Color): ShapeMap['rectangle'];
export function createShape(type: 'square', width: number, color: Color): ShapeMap['square'];
export function createShape(type: 'triangle', base: number, height: number, color: Color): ShapeMap['triangle'];

// calculate area only for these 3, or calculate everything [possible extension of shapes] except square?
export function calculateShapeArea(shape: Circle | Rectangle | Triangle): number;
// export function calculateShapeArea(shape: Exclude<Shape, { type: 'square' }>): number;
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
