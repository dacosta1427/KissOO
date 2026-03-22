import "clsx";
let uuid = "";
const session = {
  /**
   * Check if user is authenticated
   */
  get isAuthenticated() {
    return uuid.length > 0;
  }
};
export {
  session as s
};
