import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { login, logout, signup, isAuthenticated, initBackend } from './Auth';
import { Server } from '$lib/services/Server';
import { session } from '$lib/state/session';

// Mock the Server module
vi.mock('$lib/services/Server', () => ({
  Server: {
    call: vi.fn(),
    setURL: vi.fn()
  }
}));

// Mock the session module
vi.mock('$lib/state/session', () => ({
  session: {
    setUUID: vi.fn(),
    clear: vi.fn(),
    isAuthenticated: false
  }
}));

describe('Auth API', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('login', () => {
    it('should call Server.call with correct parameters', async () => {
      const mockResponse = { _Success: true, uuid: 'test-uuid-123' };
      vi.mocked(Server.call).mockResolvedValue(mockResponse);

      const result = await login('testuser', 'password123');

      expect(Server.call).toHaveBeenCalledWith('', 'Login', {
        username: 'testuser',
        password: 'password123'
      });
      expect(result).toEqual(mockResponse);
    });

    it('should lowercase the username before sending', async () => {
      const mockResponse = { _Success: true, uuid: 'test-uuid-123' };
      vi.mocked(Server.call).mockResolvedValue(mockResponse);

      await login('TestUser', 'password123');

      expect(Server.call).toHaveBeenCalledWith('', 'Login', {
        username: 'testuser',
        password: 'password123'
      });
    });

    it('should set session UUID on successful login', async () => {
      const mockResponse = { _Success: true, uuid: 'test-uuid-123' };
      vi.mocked(Server.call).mockResolvedValue(mockResponse);

      await login('testuser', 'password123');

      expect(session.setUUID).toHaveBeenCalledWith('test-uuid-123');
    });

    it('should not set session UUID on failed login', async () => {
      const mockResponse = { _Success: false, _ErrorMessage: 'Invalid credentials' };
      vi.mocked(Server.call).mockResolvedValue(mockResponse);

      await login('testuser', 'wrongpassword');

      expect(session.setUUID).not.toHaveBeenCalled();
    });

    it('should return the response from Server.call', async () => {
      const mockResponse = { _Success: false, _ErrorMessage: 'User not found' };
      vi.mocked(Server.call).mockResolvedValue(mockResponse);

      const result = await login('testuser', 'password123');

      expect(result).toEqual(mockResponse);
    });
  });

  describe('logout', () => {
    it('should call Server.call with Logout endpoint', async () => {
      vi.mocked(Server.call).mockResolvedValue({ _Success: true });

      await logout();

      expect(Server.call).toHaveBeenCalledWith('', 'Logout', {});
    });

    it('should clear session even if Server.call throws', async () => {
      vi.mocked(Server.call).mockRejectedValue(new Error('Network error'));

      await logout();

      expect(session.clear).toHaveBeenCalled();
    });

    it('should always clear session', async () => {
      vi.mocked(Server.call).mockResolvedValue({ _Success: true });

      await logout();

      expect(session.clear).toHaveBeenCalled();
    });
  });

  describe('signup', () => {
    it('should call Server.call with correct parameters', async () => {
      const mockResponse = { _Success: true, id: 123 };
      const loginResponse = { _Success: true, uuid: 'new-uuid-456' };
      
      vi.mocked(Server.call)
        .mockResolvedValueOnce(mockResponse)
        .mockResolvedValueOnce(loginResponse);

      await signup('newuser', 'password123');

      expect(Server.call).toHaveBeenCalledWith('services.Users', 'addRecord', {
        userName: 'newuser',
        userPassword: 'password123',
        userActive: 'Y'
      });
    });

    it('should lowercase the username', async () => {
      const mockResponse = { _Success: true, id: 123 };
      const loginResponse = { _Success: true, uuid: 'new-uuid-456' };
      
      vi.mocked(Server.call)
        .mockResolvedValueOnce(mockResponse)
        .mockResolvedValueOnce(loginResponse);

      await signup('NewUser', 'password123');

      expect(Server.call).toHaveBeenCalledWith('services.Users', 'addRecord', {
        userName: 'newuser',
        userPassword: 'password123',
        userActive: 'Y'
      });
    });

    it('should auto-login after successful signup', async () => {
      const mockResponse = { _Success: true, id: 123 };
      const loginResponse = { _Success: true, uuid: 'new-uuid-456' };
      
      vi.mocked(Server.call)
        .mockResolvedValueOnce(mockResponse)
        .mockResolvedValueOnce(loginResponse);

      await signup('newuser', 'password123');

      expect(Server.call).toHaveBeenCalledTimes(2);
      expect(Server.call).toHaveBeenLastCalledWith('', 'Login', {
        username: 'newuser',
        password: 'password123'
      });
    });

    it('should return error if signup fails', async () => {
      const mockResponse = { _Success: false, _ErrorMessage: 'Username already exists' };
      vi.mocked(Server.call).mockResolvedValue(mockResponse);

      const result = await signup('existinguser', 'password123');

      expect(result._Success).toBe(false);
      expect(result._ErrorMessage).toBe('Username already exists');
    });

    it('should handle alternative success response format', async () => {
      const mockResponse = { success: true, id: 123 };
      const loginResponse = { _Success: true, uuid: 'new-uuid-456' };
      
      vi.mocked(Server.call)
        .mockResolvedValueOnce(mockResponse)
        .mockResolvedValueOnce(loginResponse);

      await signup('newuser', 'password123');

      expect(Server.call).toHaveBeenCalledTimes(2);
    });
  });

  describe('isAuthenticated', () => {
    it('should return session.isAuthenticated value', () => {
      // Default mock returns false
      expect(isAuthenticated()).toBe(false);
    });
  });

  describe('initBackend', () => {
    const originalWindow = global.window;

    beforeEach(() => {
      vi.clearAllMocks();
    });

    afterEach(() => {
      global.window = originalWindow;
    });

    it('should do nothing if window is undefined', () => {
      // @ts-expect-error - testing SSR scenario
      delete global.window;
      
      initBackend();

      expect(Server.setURL).not.toHaveBeenCalled();
    });

    it('should set localhost URL for file protocol', () => {
      global.window = {
        location: {
          protocol: 'file:',
          port: ''
        }
      } as any;

      initBackend();

      expect(Server.setURL).toHaveBeenCalledWith('http://localhost:8080');
    });

    it('should set localhost URL for dev server ports', () => {
      global.window = {
        location: {
          protocol: 'http:',
          port: '5173'
        }
      } as any;

      initBackend();

      expect(Server.setURL).toHaveBeenCalledWith('http://localhost:8080');
    });

    it('should set localhost URL for port 5174', () => {
      global.window = {
        location: {
          protocol: 'http:',
          port: '5174'
        }
      } as any;

      initBackend();

      expect(Server.setURL).toHaveBeenCalledWith('http://localhost:8080');
    });

    it('should use current origin for production', () => {
      global.window = {
        location: {
          protocol: 'https:',
          port: '443',
          origin: 'https://example.com'
        }
      } as any;

      initBackend();

      expect(Server.setURL).toHaveBeenCalledWith('https://example.com');
    });
  });
});
