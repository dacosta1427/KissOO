class Server {
  static url = "";
  static uuid = "";
  static suspendDepth = 0;
  /**
   * Set the backend URL
   * @param url - Backend server URL
   */
  static setURL(url) {
    Server.url = url;
  }
  /**
   * Get the current backend URL
   * @returns Current backend URL
   */
  static getURL() {
    return Server.url;
  }
  /**
   * Set the user UUID
   * @param uuid - User session UUID
   */
  static setUUID(uuid) {
    Server.uuid = uuid;
  }
  /**
   * Make a remote call to the backend
   * @param cls - Class name of the backend service
   * @param meth - Method name to call
   * @param injson - JSON data to send
   * @returns Promise with the response data
   */
  static async call(cls, meth, injson = {}) {
    if (!Server.url) {
      throw new Error("Server URL not set");
    }
    const payload = {
      ...injson,
      _uuid: Server.uuid,
      _method: meth,
      _class: cls
    };
    try {
      Server.incCount();
      const response = await fetch(`${Server.url}/rest`, {
        method: "POST",
        cache: "no-store",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify(payload)
      });
      const result = await response.json();
      Server.decCount();
      if (!result._Success) {
        if (result._ErrorCode === 2) {
          await this.handleSessionError(result._ErrorMessage);
        } else {
          throw new Error(result._ErrorMessage);
        }
      }
      return result;
    } catch (error) {
      Server.decCount();
      throw new Error(`Server communication error: ${error instanceof Error ? error.message : "Unknown error"}`);
    }
  }
  /**
   * Increment the suspend depth and show loading cursor
   */
  static incCount() {
    if (++Server.suspendDepth === 1) {
      document.body.style.cursor = "wait";
    }
  }
  /**
   * Decrement the suspend depth and restore cursor
   */
  static decCount() {
    if (--Server.suspendDepth === 0) {
      document.body.style.cursor = "default";
    }
  }
  /**
   * Handle session errors (logout, etc.)
   * @param message - Error message
   */
  static async handleSessionError(message) {
    console.error("Session error:", message);
    throw new Error(message);
  }
  /**
   * Logout the current user
   */
  static async logout() {
    Server.suspendDepth = 0;
    document.body.style.cursor = "default";
    if (Server.uuid) {
      try {
        await Server.call("", "Logout", {});
      } catch (err) {
        console.log("Logout service call failed:", err);
      }
    }
    Server.uuid = "";
    console.log("User logged out");
  }
  // Class variables
  static suspendDepth = 0;
}
export {
  Server as S
};
