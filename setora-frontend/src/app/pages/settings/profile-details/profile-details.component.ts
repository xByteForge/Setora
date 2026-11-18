import {
  ChangeDetectionStrategy,
  Component,
  computed,
  effect,
  inject,
  input,
  signal,
} from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { TranslocoModule } from '@ngneat/transloco';
import { User } from '../../../core/models/user.model';
import { UserService } from '../../../core/services/user.service';

@Component({
  selector: 'app-profile-details',
  imports: [ReactiveFormsModule, TranslocoModule],
  templateUrl: './profile-details.component.html',
  styleUrls: ['../settings-shared.css', './profile-details.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProfileDetailsComponent {
  private readonly fb = inject(FormBuilder);
  private readonly userService = inject(UserService);

  readonly user = input.required<User>();
  readonly editing = signal(false);

  readonly initials = computed(() => {
    const u = this.user();
    return `${u.firstName.charAt(0)}${u.lastName.charAt(0)}`.toUpperCase();
  });

  readonly form = this.fb.nonNullable.group({
    firstName: '',
    lastName: '',
    username: '',
    phoneNumber: '',
  });

  constructor() {
    effect(() => this.patchForm(this.user()));
  }

  startEditing(): void {
    this.editing.set(true);
  }

  cancel(): void {
    this.patchForm(this.user());
    this.editing.set(false);
  }

  save(): void {
    this.userService.updateProfile(this.form.getRawValue()).subscribe();
    this.editing.set(false);
  }

  changePhoto(): void {
    // TODO: file picker, then userService.uploadAvatar(file). The User model has no avatar field yet.
  }

  private patchForm(u: User): void {
    this.form.reset({
      firstName: u.firstName,
      lastName: u.lastName,
      username: u.username,
      phoneNumber: u.phoneNumber ?? '',
    });
  }
}