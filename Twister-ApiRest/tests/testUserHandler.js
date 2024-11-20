import { getUserById } from '../src/services/userService';
jest.mock('../src/services/userService');

test('Get user by ID', async () => {
  getUserById.mockResolvedValue({ userId: '123', name: 'John Doe' });
  const response = await getUserById('123');
  expect(response).toEqual({ userId: '123', name: 'John Doe' });
});
