export interface UpdateUserRequest {
  firstName: string;
  lastName: string;
  username: string;
  email: string;
  phoneNumber: string | null;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

export interface AvatarUploadResponse {
  id: number;
}
