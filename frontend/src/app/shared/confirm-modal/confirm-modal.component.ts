import { Component, computed, input, output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-confirm-modal',
  standalone: true,
  imports: [FormsModule],
  styleUrl : './confirm-modal.component.scss',
  template: `
    @if (open()) {
      <div class="modal-backdrop" (click)="onCancel()">
        <div
          class="modal"
          role="dialog"
          aria-modal="true"
          [attr.aria-label]="title()"
          (click)="$event.stopPropagation()"
        >
          <h2 class="modal-title" [class.danger]="variant() === 'danger'">
            {{ title() }}
          </h2>

          <p class="modal-message">{{ message() }}</p>

          @if (confirmText()) {
            <label class="modal-confirm-label">
              Type <strong>{{ confirmText() }}</strong> to confirm
              <input
                type="text"
                [ngModel]="typedText()"
                (ngModelChange)="typedText.set($event)"
                [disabled]="busy()"
                autocomplete="off"
              />
            </label>
          }

          <div class="modal-actions">
            <button
              type="button"
              class="btn btn-secondary"
              (click)="onCancel()"
              [disabled]="busy()"
            >
              Cancel
            </button>
            <button
              type="button"
              class="btn"
              [class.btn-danger]="variant() === 'danger'"
              [class.btn-primary]="variant() === 'default'"
              (click)="onConfirm()"
              [disabled]="busy() || !canConfirm()"
            >
              {{ busy() ? 'Working…' : confirmLabel() }}
            </button>
          </div>
        </div>
      </div>
    }
  `,
})
export class ConfirmModalComponent {
  open = input.required<boolean>();
  title = input.required<string>();
  message = input.required<string>();
  confirmLabel = input<string>('Confirm');
  variant = input<'default' | 'danger'>('default');
  confirmText = input<string | null>(null);
  busy = input<boolean>(false);

  confirmed = output<void>();
  cancelled = output<void>();

  typedText = signal('');

  canConfirm = computed(() => {
    const required = this.confirmText();
    return !required || this.typedText().trim() === required;
  });

  onConfirm(): void {
    if (!this.canConfirm() || this.busy()) return;
    this.typedText.set('');
    this.confirmed.emit();
  }

  onCancel(): void {
    if (this.busy()) return;
    this.typedText.set('');
    this.cancelled.emit();
  }
}
