import { ComponentFixture, TestBed } from "@angular/core/testing";
import { AppointmentsComponent } from "./appointments.component";
import { DEFAULT_FILTERS } from "../../core/models/appointment-filters.model";

describe("AppointmentsComponent", () => {
  let component: AppointmentsComponent;
  let fixture: ComponentFixture<AppointmentsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppointmentsComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(AppointmentsComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it("should create", () => {
    expect(component).toBeTruthy();
  });

  it("shows all 10 mock appointments with default filters", () => {
    expect(component.visibleAppointments().length).toBe(10);
  });

  it("matches the summary shown in the design (9 / 7 / 1 / 1)", () => {
    expect(component.summary()).toEqual({ total: 9, confirmed: 7, pending: 1, cancelled: 1 });
  });

  it("filters by employee", () => {
    component.filters.set({ ...DEFAULT_FILTERS, employeeId: "ana" });
    expect(component.visibleAppointments().map((a) => a.clientName)).toEqual([
      "Irina Popescu",
      "Elena Dobre",
      "Maria Șerban",
    ]);
    expect(component.visibleEmployees().length).toBe(1);
  });

  it("combines status and time range", () => {
    component.filters.set({
      ...DEFAULT_FILTERS,
      status: "confirmed",
      timeRange: { from: 13 * 60, to: 17 * 60 },
    });
    expect(component.visibleAppointments().map((a) => a.clientName)).toEqual([
      "Maria Șerban",
      "Vlad Marinescu",
      "Oana Dumitru",
    ]);
  });

  it("keeps summary counts for other statuses when a status filter is active", () => {
    component.filters.set({ ...DEFAULT_FILTERS, status: "pending" });
    expect(component.summary().confirmed).toBe(7);
  });

  it("only lists employees from the selected location", () => {
    component.filters.set({ ...DEFAULT_FILTERS, locationId: "buc-nord" });
    expect(component.visibleEmployees().map((e) => e.id)).toEqual(["ioana"]);
    expect(component.visibleAppointments().length).toBe(0);
  });
});