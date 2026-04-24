import { s as session } from "./session.svelte.js";
import "clsx";
import "@sveltejs/kit/internal";
import "./exports.js";
import "./utils.js";
import "@sveltejs/kit/internal/server";
import "./root.js";
import "./state.svelte.js";
class Server {
  static url = "";
  static uuid = "";
  static suspendDepth = 0;
  static timeLastCall = 0;
  static maxInactiveSeconds = 0;
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
    await Server.checkTime();
    const payload = {
      ...injson,
      _uuid: Server.uuid,
      _method: meth,
      _class: cls
    };
    if (cls === "services.CleaningService") {
      console.log("[Server] CleaningService service call:", meth, "UUID:", Server.uuid);
    }
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
   * Perform a binary call (JSON sent, binary data returned in _data field)
   * @param cls - Class name of the backend service
   * @param meth - Method name to call
   * @param injson - JSON data to send
   * @returns Promise with response containing _data field for binary content
   */
  static async binaryCall(cls, meth, injson = {}) {
    if (!Server.url) {
      throw new Error("Server URL not set");
    }
    await Server.checkTime();
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
        body: JSON.stringify(payload),
        headers: {
          "Content-Type": "application/json"
        }
      });
      const buffer = await response.arrayBuffer();
      Server.decCount();
      const bytes = new Uint8Array(buffer);
      let json = "";
      let i = 0;
      const m = bytes.length;
      while (i < m && bytes[i] !== 3) {
        json += String.fromCharCode(bytes[i]);
        i++;
      }
      const result = JSON.parse(json);
      if (!result._Success) {
        if (result._ErrorCode === 2) {
          await this.handleSessionError(result._ErrorMessage);
        } else {
          throw new Error(result._ErrorMessage);
        }
      }
      result._data = bytes.slice(i, bytes.length);
      return result;
    } catch (error) {
      Server.decCount();
      throw new Error(`Server binary communication error: ${error instanceof Error ? error.message : "Unknown error"}`);
    }
  }
  /**
   * Send file upload to the server
   * @param cls - Class name of the backend service
   * @param meth - Method name to call
   * @param fd - FormData, FileList, array of FileList, or control ID string
   * @param injson - Additional JSON data to send
   * @param waitMsg - Optional wait message
   * @param successMessage - Optional success message
   * @returns Promise with the response data
   */
  static async fileUploadSend(cls, meth, fd, injson = {}, waitMsg, successMessage) {
    if (!Server.url) {
      throw new Error("Server URL not set");
    }
    await Server.checkTime();
    let formData;
    if (typeof fd === "string") {
      throw new Error("Control ID not supported in Svelte. Pass FormData directly.");
    } else if (fd instanceof FormData) {
      formData = fd;
    } else if (fd instanceof FileList) {
      formData = new FormData();
      for (let i = 0; i < fd.length; i++) {
        formData.append(`_file-${i}`, fd[i]);
      }
    } else if (Array.isArray(fd)) {
      formData = new FormData();
      let i = 0;
      for (const files of fd) {
        if (Array.isArray(files)) {
          for (const file of files) {
            formData.append(`_file-${i++}`, file);
          }
        } else {
          formData.append(`_file-${i++}`, files);
        }
      }
    } else {
      throw new Error("Invalid fd parameter type");
    }
    formData.append("_class", cls);
    formData.append("_method", meth);
    formData.append("_uuid", Server.uuid);
    if (injson) {
      for (const key in injson) {
        let val = injson[key];
        if (typeof val === "object" && val !== null) {
          val = JSON.stringify(val);
        } else if (typeof val === "string") {
          val = "S" + val;
        }
        formData.append(key, val);
      }
    }
    if (waitMsg) {
      console.log(waitMsg);
    }
    try {
      Server.incCount();
      const response = await fetch(`${Server.url}/rest`, {
        method: "POST",
        cache: "no-store",
        body: formData
        // Browser sets Content-Type automatically
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
      if (successMessage) {
        console.log(successMessage);
      }
      return result;
    } catch (error) {
      Server.decCount();
      throw new Error(`Server file upload error: ${error instanceof Error ? error.message : "Unknown error"}`);
    }
  }
  /**
   * Call multiple services in parallel and wait for all to complete
   * @param promises - Array of promises from service calls
   * @param handlers - Optional array of result handlers for each promise
   * @returns Promise that resolves to false if all succeed, true if any error
   */
  static async callAll(promises, handlers) {
    await Server.checkTime();
    return new Promise((resolve) => {
      Promise.all(promises).then((results) => {
        for (const result of results) {
          if (!result._Success) {
            if (result._ErrorCode === 2) {
              Server.logout();
            }
            resolve(true);
            return;
          }
        }
        if (handlers) {
          for (let i = 0; i < handlers.length && i < results.length; i++) {
            const handler = handlers[i];
            if (handler) {
              handler(results[i]);
            }
          }
        }
        resolve(false);
      }).catch(() => {
        resolve(true);
      });
    });
  }
  /**
   * Set maximum seconds of inactivity before auto-logout
   * @param seconds - Maximum seconds of inactivity (0 = no limit)
   */
  static setMaxInactivitySeconds(seconds) {
    Server.maxInactiveSeconds = seconds;
    Server.timeLastCall = Math.floor(Date.now() / 1e3);
  }
  /**
   * Set maximum minutes of inactivity before auto-logout
   * @param minutes - Maximum minutes of inactivity
   */
  static setMaxInactivityMinutes(minutes) {
    Server.setMaxInactivitySeconds(minutes * 60);
  }
  /**
   * Set maximum hours of inactivity before auto-logout
   * @param hours - Maximum hours of inactivity
   */
  static setMaxInactivityHours(hours) {
    Server.setMaxInactivitySeconds(hours * 60 * 60);
  }
  /**
   * Check inactivity timeout
   */
  static async checkTime() {
    if (!Server.maxInactiveSeconds) {
      return;
    }
    const now = Math.floor(Date.now() / 1e3);
    if (now - Server.timeLastCall > Server.maxInactiveSeconds) {
      console.error("Session expired due to inactivity");
      await Server.logout();
    } else {
      Server.timeLastCall = now;
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
    const credentials = await session.getStoredCredentials();
    if (credentials) {
      console.log("Attempting silent re-authentication...");
      try {
        const res = await login(credentials.username, credentials.password);
        if (res._Success && res.uuid) {
          session.setUUID(res.uuid);
          Server.uuid = res.uuid;
          console.log("Silent re-authentication successful");
          return;
        }
      } catch (loginError) {
        console.error("Silent re-authentication failed:", loginError);
      }
    }
    session.clearCredentials();
    session.clear();
    if (typeof window !== "undefined") {
      window.location.href = "/";
    }
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
    session.clear();
    session.clearCredentials();
    console.log("User logged out");
  }
}
async function login(usernameInput, password) {
  const res = await Server.call("", "Login", {
    username: usernameInput.toLowerCase(),
    password
  });
  if (res._Success && res.uuid) {
    session.setUUID(res.uuid);
    session.setUsername(usernameInput.toLowerCase());
    Server.setUUID(res.uuid);
    if (res.userOid) {
      session.setUserOid(res.userOid);
    }
    if (res.isAdmin !== void 0) {
      session.setIsAdmin(res.isAdmin);
    }
    if (res.adminType) {
      session.setAdminType(res.adminType);
    }
    if (res.ownerOid) {
      session.setOwnerOid(res.ownerOid);
    }
    if (res.ownerName) {
      session.setOwnerName(res.ownerName);
    }
    if (res.cleanerOid) {
      session.setCleanerOid(res.cleanerOid);
    }
    if (res.email) {
      session.setEmail(res.email);
    }
    if (res.preferredLanguage) {
      session.setLanguage(res.preferredLanguage);
    }
    if (res.needsPasswordChange !== void 0) {
      session.setNeedsPasswordChange(res.needsPasswordChange);
    }
    if (res.needsEmailVerification !== void 0) {
      session.setNeedsEmailVerification(res.needsEmailVerification);
    }
    if (res.fullyActivated !== void 0) {
      session.setFullyActivated(res.fullyActivated);
    }
  }
  return res;
}
function isAuthenticated() {
  return session.isAuthenticated;
}
export {
  Server as S,
  isAuthenticated as i
};
