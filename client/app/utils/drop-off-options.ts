export interface DropOffOption {
  key: string;
  label: string;
  latitude: number;
  longitude: number;
}

export interface ReservationDestination {
  latitude: number;
  longitude: number;
}

const PREDEFINED_ZONE_OPTIONS: DropOffOption[] = [
  {
    key: "zone:downtown",
    label: "Montreal Downtown Drop-off Zone",
    latitude: 45.5100,
    longitude: -73.5650,
  },
  {
    key: "zone:verdun",
    label: "Verdun Drop-off Zone",
    latitude: 45.4550,
    longitude: -73.5750,
  },
  {
    key: "zone:plateau",
    label: "Plateau Drop-off Zone",
    latitude: 45.5350,
    longitude: -73.5850,
  },
];

export function buildDropOffOptions(
  reservationDestination: ReservationDestination | null,
  reservationCity: string | null,
): DropOffOption[] {
  const options: DropOffOption[] = [];

  if (
    reservationDestination &&
    Number.isFinite(reservationDestination.latitude) &&
    Number.isFinite(reservationDestination.longitude)
  ) {
    const cityLabel = reservationCity?.trim().length
      ? ` (${reservationCity.trim()})`
      : "";
    options.push({
      key: "reservation:end-location",
      label: `Reserved destination${cityLabel}`,
      latitude: reservationDestination.latitude,
      longitude: reservationDestination.longitude,
    });
  }

  return [...options, ...PREDEFINED_ZONE_OPTIONS];
}

