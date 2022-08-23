export class JSONSet extends Set {
  toJSON() {
    return [...this];
  }
}
