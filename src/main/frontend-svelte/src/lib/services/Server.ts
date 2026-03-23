/**
 * Server service for communicating with the backend
 * Implements the Server.call(...) pattern using modern fetch API
 */
import { session } from '$lib/state/session';
import { login } from '$lib/api/Auth';

export class Server {
  private static url: string = '';
  private static uuid: string = '';
  private static suspendDepth: number = 0;
  private static timeLastCall: number = 0;
  private static maxInactiveSeconds: number = 0;

  /**
   * Set the backend URL
   * @param url - Backend server URL
   */
  static setURL(url: string): void {
    Server.url = url;
  }

  /**
   * Get the current backend URL
   * @returns Current backend URL
   */
  static getURL(): string {
    return Server.url;
  }

  /**
   * Set the user UUID
   * @param uuid - User session UUID
   */
  static setUUID(uuid: string): void {
    Server.uuid = uuid;
  }

  /**
   * Make a remote call to the backend
   * @param cls - Class name of the backend service
   * @param meth - Method name to call
   * @param injson - JSON data to send
   * @returns Promise with the response data
   */
  static async call(cls: string, meth: string, injson: any = {}): Promise<any> {
    if (!Server.url) {
      throw new Error('Server URL not set');
    }

    await Server.checkTime();

    // Add required parameters
    const payload = {
      ...injson,
      _uuid: Server.uuid,
      _method: meth,
      _class: cls
    };

    try {
      Server.incCount();
      const response = await fetch(`${Server.url}/rest`, {
        method: 'POST',
        cache: 'no-store',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload)
      });

      const result = await response.json();
      Server.decCount();

      if (!result._Success) {
        if (result._ErrorCode === 2) {
          // Session expired or invalid
          await this.handleSessionError(result._ErrorMessage);
        } else {
          throw new Error(result._ErrorMessage);
        }
      }

      return result;
    } catch (error) {
      Server.decCount();
      throw new Error(`Server communication error: ${error instanceof Error ? error.message : 'Unknown error'}`);
    }
  }

  /**
   * Perform a binary call (JSON sent, binary data returned in _data field)
   * @param cls - Class name of the backend service
   * @param meth - Method name to call
   * @param injson - JSON data to send
   * @returns Promise with response containing _data field for binary content
   */
  static async binaryCall(cls: string, meth: string, injson: any = {}): Promise<any> {
    if (!Server.url) {
      throw new Error('Server URL not set');
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
        method: 'POST',
        cache: 'no-store',
        body: JSON.stringify(payload),
        headers: {
          'Content-Type': 'application/json'
        }
      });

      const buffer = await response.arrayBuffer();
      Server.decCount();

      // Parse the binary response (JSON terminated with ETX character)
      const bytes = new Uint8Array(buffer);
      let json = '';
      let i = 0;
      const m = bytes.length;
      while (i < m && bytes[i] !== 0x03) { // ETX character
        json += String.fromCharCode(bytes[i]!);
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
      
      // Add binary data to result
      result._data = bytes.slice(i, bytes.length);
      return result;
    } catch (error) {
      Server.decCount();
      throw new Error(`Server binary communication error: ${error instanceof Error ? error.message : 'Unknown error'}`);
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
  static async fileUploadSend(
    cls: string,
    meth: string,
    fd: FormData | FileList | string | any[],
    injson: any = {},
    waitMsg?: string,
    successMessage?: string
  ): Promise<any> {
    if (!Server.url) {
      throw new Error('Server URL not set');
    }

    await Server.checkTime();

    // Convert fd to FormData if needed
    let formData: FormData;
    if (typeof fd === 'string') {
      // Control ID - not implemented in Svelte, should pass FormData directly
      throw new Error('Control ID not supported in Svelte. Pass FormData directly.');
    } else if (fd instanceof FormData) {
      formData = fd;
    } else if (fd instanceof FileList) {
      formData = new FormData();
      for (let i = 0; i < fd.length; i++) {
        formData.append(`_file-${i}`, fd[i]!);
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
      throw new Error('Invalid fd parameter type');
    }

    // Add required parameters to FormData
    formData.append('_class', cls);
    formData.append('_method', meth);
    formData.append('_uuid', Server.uuid);

    // Add additional JSON data
    if (injson) {
      for (const key in injson) {
        let val = injson[key];
        if (typeof val === 'object' && val !== null) {
          val = JSON.stringify(val);
        } else if (typeof val === 'string') {
          val = 'S' + val; // Prefix strings with 'S' as per legacy convention
        }
        formData.append(key, val);
      }
    }

    // Show wait message if provided
    if (waitMsg) {
      console.log(waitMsg); // TODO: Show wait overlay
    }

    try {
      Server.incCount();
      const response = await fetch(`${Server.url}/rest`, {
        method: 'POST',
        cache: 'no-store',
        body: formData // Browser sets Content-Type automatically
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
        console.log(successMessage); // TODO: Show success message via modal
      }

      return result;
    } catch (error) {
      Server.decCount();
      throw new Error(`Server file upload error: ${error instanceof Error ? error.message : 'Unknown error'}`);
    }
  }

  /**
   * Call multiple services in parallel and wait for all to complete
   * @param promises - Array of promises from service calls
   * @param handlers - Optional array of result handlers for each promise
   * @returns Promise that resolves to false if all succeed, true if any error
   */
  static async callAll(
    promises: Promise<any>[],
    handlers?: Array<((result: any) => void) | null>
  ): Promise<boolean> {
    await Server.checkTime();
    
    return new Promise((resolve) => {
      Promise.all(promises).then((results) => {
        // Check for errors
        for (const result of results) {
          if (!result._Success) {
            if (result._ErrorCode === 2) {
              Server.logout();
            }
            resolve(true); // Error occurred
            return;
          }
        }
        
        // Call handlers if provided
        if (handlers) {
          for (let i = 0; i < handlers.length && i < results.length; i++) {
            const handler = handlers[i];
            if (handler) {
              handler(results[i]);
            }
          }
        }
        
        resolve(false); // All succeeded
      }).catch(() => {
        resolve(true); // Promise.all error
      });
    });
  }

  /**
   * Set maximum seconds of inactivity before auto-logout
   * @param seconds - Maximum seconds of inactivity (0 = no limit)
   */
  static setMaxInactivitySeconds(seconds: number): void {
    Server.maxInactiveSeconds = seconds;
    Server.timeLastCall = Math.floor(Date.now() / 1000);
  }

  /**
   * Set maximum minutes of inactivity before auto-logout
   * @param minutes - Maximum minutes of inactivity
   */
  static setMaxInactivityMinutes(minutes: number): void {
    Server.setMaxInactivitySeconds(minutes * 60);
  }

  /**
   * Set maximum hours of inactivity before auto-logout
   * @param hours - Maximum hours of inactivity
   */
  static setMaxInactivityHours(hours: number): void {
    Server.setMaxInactivitySeconds(hours * 60 * 60);
  }

  /**
   * Check inactivity timeout
   */
  private static async checkTime(): Promise<void> {
    if (!Server.maxInactiveSeconds) {
      return;
    }
    
    const now = Math.floor(Date.now() / 1000);
    if (now - Server.timeLastCall > Server.maxInactiveSeconds) {
      console.error('Session expired due to inactivity');
      await Server.logout();
    } else {
      Server.timeLastCall = now;
    }
  }

  /**
   * Increment the suspend depth and show loading cursor
   */
  private static incCount(): void {
    if (++Server.suspendDepth === 1) {
      document.body.style.cursor = 'wait';
    }
  }

  /**
   * Decrement the suspend depth and restore cursor
   */
  private static decCount(): void {
    if (--Server.suspendDepth === 0) {
      document.body.style.cursor = 'default';
    }
  }

  /**
   * Handle session errors (logout, etc.)
   * @param message - Error message
   */
  private static async handleSessionError(message: string): Promise<void> {
    console.error('Session error:', message);
    
    // Attempt silent re-authentication if credentials are stored
    const credentials = await session.getStoredCredentials();
    if (credentials) {
      console.log('Attempting silent re-authentication...');
      try {
        const res = await login(credentials.username, credentials.password);
        if (res._Success && res.uuid) {
          // Update session with new UUID
          session.setUUID(res.uuid);
          Server.uuid = res.uuid;
          console.log('Silent re-authentication successful');
          return; // Don't throw - let the original call continue
        }
      } catch (loginError) {
        console.error('Silent re-authentication failed:', loginError);
      }
    }
    
    // If we get here, either no credentials or re-auth failed
    // Clear stored credentials and redirect to login
    session.clearCredentials();
    session.clear();
    
    // Redirect to login with error message
    if (typeof window !== 'undefined') {
      window.location.href = '/login?expired=true';
    }
    
    throw new Error(message);
  }

  /**
   * Logout the current user
   */
  static async logout(): Promise<void> {
    Server.suspendDepth = 0;
    document.body.style.cursor = 'default';

    // Call backend Logout service if we have a valid UUID
    if (Server.uuid) {
      try {
        await Server.call('', 'Logout', {});
      } catch (err) {
        console.log('Logout service call failed:', err);
      }
    }

    Server.uuid = '';
    session.clear();
    session.clearCredentials();
    console.log('User logged out');
    // In a real app, redirect to login page
  }
}