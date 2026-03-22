/**
 * Server service for communicating with the backend
 * Implements the Server.call(...) pattern using modern fetch API
 */
export class Server {
  private static url: string = '';
  private static uuid: string = '';
  private static suspendDepth: number = 0;

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
    // In a real implementation, you might want to redirect to login
    console.error('Session error:', message);
    // For now, just throw the error
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
    // In a real app, you might want to redirect to login page
    console.log('User logged out');
  }

  // Class variables
  private static suspendDepth: number = 0;
}