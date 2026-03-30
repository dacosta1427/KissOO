import { describe, it, expect, vi, beforeEach } from 'vitest';
import { getUsers, addUser, deleteUser, updateUser } from './Users';
import { Server } from '$lib/services/Server';

// Mock the Server module
vi.mock('$lib/services/Server', () => ({
  Server: {
    call: vi.fn()
  }
}));

describe('Users API', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('getUsers', () => {
    it('should call Server.call with correct parameters', async () => {
      const mockResponse = { rows: [] };
      vi.mocked(Server.call).mockResolvedValue(mockResponse);

      await getUsers();

      expect(Server.call).toHaveBeenCalledWith('services.Users', 'getUsers', {});
    });

    it('should return users array from response', async () => {
      const mockUsers = [
        { id: 1, userName: 'user1', userPassword: '', userActive: 'Y' as const },
        { id: 2, userName: 'user2', userPassword: '', userActive: 'Y' as const }
      ];
      const mockResponse = { rows: mockUsers };
      vi.mocked(Server.call).mockResolvedValue(mockResponse);

      const result = await getUsers();

      expect(result).toEqual(mockUsers);
    });

    it('should return empty array if no rows', async () => {
      const mockResponse = {};
      vi.mocked(Server.call).mockResolvedValue(mockResponse);

      const result = await getUsers();

      expect(result).toEqual([]);
    });

    it('should return empty array if rows is undefined', async () => {
      const mockResponse = { rows: undefined };
      vi.mocked(Server.call).mockResolvedValue(mockResponse);

      const result = await getUsers();

      expect(result).toEqual([]);
    });
  });

  describe('addUser', () => {
    it('should call Server.call with correct parameters', async () => {
      const mockResponse = { _Success: true, id: 123 };
      vi.mocked(Server.call).mockResolvedValue(mockResponse);

      await addUser('newuser', 'password123');

      expect(Server.call).toHaveBeenCalledWith('services.Users', 'createUser', {
        userName: 'newuser',
        userPassword: 'password123',
        userActive: 'Y'
      });
    });

    it('should lowercase the username', async () => {
      const mockResponse = { _Success: true, id: 123 };
      vi.mocked(Server.call).mockResolvedValue(mockResponse);

      await addUser('NewUser', 'password123');

      expect(Server.call).toHaveBeenCalledWith('services.Users', 'createUser', {
        userName: 'newuser',
        userPassword: 'password123',
        userActive: 'Y'
      });
    });

    it('should return success result', async () => {
      const mockResponse = { _Success: true, id: 123 };
      vi.mocked(Server.call).mockResolvedValue(mockResponse);

      const result = await addUser('newuser', 'password123');

      expect(result.success).toBe(true);
      expect(result.id).toBe(123);
    });

    it('should return error result on failure', async () => {
      const mockResponse = { _Success: false, _ErrorMessage: 'Username exists' };
      vi.mocked(Server.call).mockResolvedValue(mockResponse);

      const result = await addUser('existinguser', 'password123');

      expect(result.success).toBe(false);
      expect(result.error).toBe('Username exists');
    });

    it('should handle alternative success format', async () => {
      const mockResponse = { success: true, id: 456 };
      vi.mocked(Server.call).mockResolvedValue(mockResponse);

      const result = await addUser('newuser', 'password123');

      expect(result.success).toBe(true);
      expect(result.id).toBe(456);
    });

    it('should handle alternative error format', async () => {
      const mockResponse = { success: false, error: 'Database error' };
      vi.mocked(Server.call).mockResolvedValue(mockResponse);

      const result = await addUser('newuser', 'password123');

      expect(result.success).toBe(false);
      expect(result.error).toBe('Database error');
    });
  });

  describe('deleteUser', () => {
    it('should call Server.call with correct parameters', async () => {
      const mockResponse = { _Success: true };
      vi.mocked(Server.call).mockResolvedValue(mockResponse);

      await deleteUser(123);

      expect(Server.call).toHaveBeenCalledWith('services.Users', 'deleteUser', { id: 123 });
    });

    it('should return success result', async () => {
      const mockResponse = { _Success: true };
      vi.mocked(Server.call).mockResolvedValue(mockResponse);

      const result = await deleteUser(123);

      expect(result.success).toBe(true);
    });

    it('should return error result on failure', async () => {
      const mockResponse = { _Success: false, _ErrorMessage: 'User not found' };
      vi.mocked(Server.call).mockResolvedValue(mockResponse);

      const result = await deleteUser(999);

      expect(result.success).toBe(false);
      expect(result.error).toBe('User not found');
    });

    it('should handle alternative success format', async () => {
      const mockResponse = { success: true };
      vi.mocked(Server.call).mockResolvedValue(mockResponse);

      const result = await deleteUser(123);

      expect(result.success).toBe(true);
    });
  });

  describe('updateUser', () => {
    it('should call Server.call with correct parameters', async () => {
      const mockResponse = { _Success: true };
      vi.mocked(Server.call).mockResolvedValue(mockResponse);

      await updateUser(123, 'updateduser', 'newpassword', 'Y');

      expect(Server.call).toHaveBeenCalledWith('services.Users', 'updateUser', {
        id: 123,
        userName: 'updateduser',
        userPassword: 'newpassword',
        userActive: 'Y'
      });
    });

    it('should lowercase the username', async () => {
      const mockResponse = { _Success: true };
      vi.mocked(Server.call).mockResolvedValue(mockResponse);

      await updateUser(123, 'UpdatedUser', 'newpassword', 'N');

      expect(Server.call).toHaveBeenCalledWith('services.Users', 'updateUser', {
        id: 123,
        userName: 'updateduser',
        userPassword: 'newpassword',
        userActive: 'N'
      });
    });

    it('should handle all active status values', async () => {
      const mockResponse = { _Success: true };
      vi.mocked(Server.call).mockResolvedValue(mockResponse);

      await updateUser(123, 'user', 'password', 'Y');
      expect(Server.call).toHaveBeenLastCalledWith('services.Users', 'updateUser',
        expect.objectContaining({ userActive: 'Y' }));

      await updateUser(123, 'user', 'password', 'N');
      expect(Server.call).toHaveBeenLastCalledWith('services.Users', 'updateUser',
        expect.objectContaining({ userActive: 'N' }));
    });

    it('should return success result', async () => {
      const mockResponse = { _Success: true };
      vi.mocked(Server.call).mockResolvedValue(mockResponse);

      const result = await updateUser(123, 'user', 'password', 'Y');

      expect(result.success).toBe(true);
    });

    it('should return error result on failure', async () => {
      const mockResponse = { _Success: false, _ErrorMessage: 'User not found' };
      vi.mocked(Server.call).mockResolvedValue(mockResponse);

      const result = await updateUser(999, 'user', 'password', 'Y');

      expect(result.success).toBe(false);
      expect(result.error).toBe('User not found');
    });
  });
});
