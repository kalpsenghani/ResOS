import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { ReservationService } from '../services/reservation.service';
import {
  CreateReservationRequest,
  Reservation,
  RestaurantTable,
  UpdateReservationRequest,
} from '../../../shared/models/reservation.model';
import { ButtonComponent } from '../../../shared/components/button/button.component';
import { SelectComponent, SelectOption } from '../../../shared/components/select/select.component';

export interface ReservationFormData {
  restaurantId: string;
  tables: RestaurantTable[];
  reservation?: Reservation;
}

@Component({
  selector: 'app-reservation-form',
  imports: [ReactiveFormsModule, MatDialogModule, ButtonComponent, SelectComponent],
  template: `
    <h2 mat-dialog-title>{{ data.reservation ? 'Edit reservation' : 'New reservation' }}</h2>
    <form [formGroup]="form" mat-dialog-content class="form">
      <div class="row">
        <label class="field">
          <span class="field-label">Guest name</span>
          <input class="input" formControlName="guestName" />
        </label>
        <label class="field">
          <span class="field-label">Party size</span>
          <input class="input" type="number" min="1" formControlName="partySize" />
        </label>
      </div>

      <div class="row">
        <label class="field">
          <span class="field-label">Date</span>
          <input class="input" type="date" formControlName="reservationDate" />
        </label>
        <label class="field">
          <span class="field-label">Start time</span>
          <input class="input" type="time" formControlName="startTime" />
        </label>
      </div>

      <div class="row">
        <label class="field">
          <span class="field-label">Phone</span>
          <input class="input" formControlName="guestPhone" />
        </label>
        <label class="field">
          <span class="field-label">Email</span>
          <input class="input" type="email" formControlName="guestEmail" />
        </label>
      </div>

      <label class="field">
        <span class="field-label">Table (optional)</span>
        <app-select formControlName="tableId" [options]="tableOptions" placeholder="Auto-assign" />
      </label>

      <label class="field">
        <span class="field-label">Special requests</span>
        <textarea class="input" rows="2" formControlName="specialRequests"></textarea>
      </label>

      @if (availabilityMessage()) {
        <p class="availability" [class.unavailable]="!availabilityOk()">{{ availabilityMessage() }}</p>
      }
      @if (error()) {
        <p class="error">{{ error() }}</p>
      }
    </form>

    <div mat-dialog-actions align="end">
      <app-button variant="ghost" (clicked)="dialogRef.close()">Cancel</app-button>
      <app-button [disabled]="saving() || form.invalid" (clicked)="submit()">
        {{ saving() ? 'Saving...' : 'Save' }}
      </app-button>
    </div>
  `,
  styles: `
    .form {
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
      min-width: min(520px, 90vw);
    }

    .row {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(10rem, 1fr));
      gap: 0.75rem;
    }

    .availability {
      margin: 0;
      color: var(--color-success);
      font-size: 0.875rem;
    }

    .availability.unavailable {
      color: var(--color-danger);
    }

    .error {
      color: var(--color-danger);
      margin: 0;
    }
  `,
})
export class ReservationFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly reservationService = inject(ReservationService);
  readonly dialogRef = inject(MatDialogRef<ReservationFormComponent>);
  readonly data = inject<ReservationFormData>(MAT_DIALOG_DATA);

  readonly saving = signal(false);
  readonly error = signal<string | null>(null);
  readonly availabilityMessage = signal<string | null>(null);
  readonly availabilityOk = signal(true);

  readonly tableOptions: SelectOption[] = [
    { value: '', label: 'Auto-assign' },
    ...this.data.tables.map((table) => ({
      value: table.id,
      label: `${table.tableNumber} (seats ${table.capacity})`,
    })),
  ];

  readonly form = this.fb.nonNullable.group({
    guestName: ['', Validators.required],
    partySize: [2, [Validators.required, Validators.min(1)]],
    reservationDate: ['', Validators.required],
    startTime: ['19:00', Validators.required],
    guestPhone: [''],
    guestEmail: [''],
    tableId: [''],
    specialRequests: [''],
  });

  ngOnInit(): void {
    const reservation = this.data.reservation;
    if (reservation) {
      this.form.patchValue({
        guestName: reservation.guestName,
        partySize: reservation.partySize,
        reservationDate: reservation.reservationDate,
        startTime: reservation.startTime.slice(0, 5),
        guestPhone: reservation.guestPhone ?? '',
        guestEmail: reservation.guestEmail ?? '',
        tableId: reservation.tableId ?? '',
        specialRequests: reservation.specialRequests ?? '',
      });
    }

    this.form.valueChanges.subscribe(() => this.checkAvailability());
  }

  private checkAvailability(): void {
    const value = this.form.getRawValue();
    if (!value.reservationDate || !value.startTime || value.partySize < 1) {
      this.availabilityMessage.set(null);
      return;
    }

    this.reservationService
      .checkAvailability({
        restaurantId: this.data.restaurantId,
        date: value.reservationDate,
        partySize: value.partySize,
        startTime: value.startTime,
      })
      .subscribe({
        next: (response) => {
          const available = response.data.available;
          this.availabilityOk.set(available);
          if (available) {
            const count = response.data.suggestedTables.length;
            this.availabilityMessage.set(`${count} table${count === 1 ? '' : 's'} available at this time`);
          } else {
            this.availabilityMessage.set('No tables available for this party size and time');
          }
        },
        error: () => this.availabilityMessage.set(null),
      });
  }

  submit(): void {
    if (this.form.invalid) return;
    this.saving.set(true);
    this.error.set(null);
    const value = this.form.getRawValue();
    const tableId = value.tableId || undefined;

    if (this.data.reservation) {
      const request: UpdateReservationRequest = {
        guestName: value.guestName,
        partySize: value.partySize,
        reservationDate: value.reservationDate,
        startTime: value.startTime,
        guestPhone: value.guestPhone || undefined,
        guestEmail: value.guestEmail || undefined,
        tableId,
        specialRequests: value.specialRequests || undefined,
      };
      this.reservationService
        .updateReservation(this.data.reservation.id, request, this.data.reservation.version)
        .subscribe({
          next: () => this.dialogRef.close(true),
          error: () => {
            this.error.set('Failed to update reservation.');
            this.saving.set(false);
          },
        });
    } else {
      const request: CreateReservationRequest = {
        restaurantId: this.data.restaurantId,
        guestName: value.guestName,
        partySize: value.partySize,
        reservationDate: value.reservationDate,
        startTime: value.startTime,
        guestPhone: value.guestPhone || undefined,
        guestEmail: value.guestEmail || undefined,
        tableId,
        specialRequests: value.specialRequests || undefined,
      };
      this.reservationService.createReservation(request).subscribe({
        next: () => this.dialogRef.close(true),
        error: () => {
          this.error.set('Failed to create reservation.');
          this.saving.set(false);
        },
      });
    }
  }
}
