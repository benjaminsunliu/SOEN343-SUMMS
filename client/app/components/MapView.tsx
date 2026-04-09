import { useEffect, useState, type ComponentType } from "react";

// Local alias for coordinates instead of importing from Leaflet types
type LatLngExpression = [number, number] | { lat: number; lng: number };

export type MarkerKind = "user" | "bicycle" | "car" | "scooter" | "vehicle" | "parking";

export interface MapMarker {
  position: LatLngExpression;
  label?: string;
  kind?: MarkerKind;
}

interface MapViewProps {
  center: LatLngExpression;
  zoom?: number;
  markers?: MapMarker[];
  className?: string;
}

interface LeafletMapProps extends MapViewProps {}

let LeafletMapImpl: ComponentType<LeafletMapProps> | null = null;

// Basic Leaflet map, rendered only on client to avoid SSR issues
export function MapView({ center, zoom = 13, markers = [], className }: MapViewProps) {
  const [isClient, setIsClient] = useState(false);
  const [ready, setReady] = useState(false);

  useEffect(() => {
    setIsClient(true);

    async function load() {
      // @ts-expect-error This project currently does not ship Leaflet type declarations.
      const L = await import("leaflet");
      await import("leaflet/dist/leaflet.css");

      const markerIcon2x = (await import("leaflet/dist/images/marker-icon-2x.png")).default as string;
      const markerIconUrl = (await import("leaflet/dist/images/marker-icon.png")).default as string;
      const markerShadow = (await import("leaflet/dist/images/marker-shadow.png")).default as string;

      // Fix default icon paths so markers render correctly after bundling
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      delete (L.Icon.Default.prototype as any)._getIconUrl;
      L.Icon.Default.mergeOptions({
        iconRetinaUrl: markerIcon2x,
        iconUrl: markerIconUrl,
        shadowUrl: markerShadow,
      });

      const reactLeaflet = await import("react-leaflet");
      const { MapContainer, Marker, Popup, TileLayer, useMap } = reactLeaflet;

      const makeIcon = (emoji: string, color: string) =>
        L.divIcon({
          className: "summs-marker-icon",
          html: `<div style="display:flex;align-items:center;justify-content:center;width:28px;height:28px;border-radius:999px;background:${color};border:2px solid #0f172a;box-shadow:0 2px 8px rgba(0,0,0,0.35);font-size:14px;">${emoji}</div>`,
          iconSize: [28, 28],
          iconAnchor: [14, 14],
          popupAnchor: [0, -12],
        });

      const markerIcons = {
        user: makeIcon("📍", "#22d3ee"),
        bicycle: makeIcon("🚲", "#22c55e"),
        scooter: makeIcon("🛴", "#f59e0b"),
        car: makeIcon("🚗", "#3b82f6"),
        parking: makeIcon("🅿", "#f97316"),
        vehicle: makeIcon("●", "#a78bfa"),
      } as const;

      function resolveMarkerIcon(kind?: MarkerKind) {
        if (kind && markerIcons[kind]) {
          return markerIcons[kind];
        }
        return markerIcons.vehicle;
      }

      function MapCenterSync({
        center,
        zoom,
      }: {
        center: LatLngExpression;
        zoom: number;
      }) {
        const map = useMap();

        useEffect(() => {
          // Keep map view synced when parent requests recentering.
          map.setView(center, zoom, { animate: true });
        }, [center, map, zoom]);

        return null;
      }

      LeafletMapImpl = function LeafletMapInner(props: LeafletMapProps) {
        const { center, zoom = 13, markers = [], className } = props;
        const MapContainerAny = MapContainer as unknown as ComponentType<any>;
        const MarkerAny = Marker as unknown as ComponentType<any>;
        const TileLayerAny = TileLayer as unknown as ComponentType<any>;

        return (
          <div className={className ?? "map-wrapper"}>
            <MapContainerAny
              center={center}
              zoom={zoom}
              scrollWheelZoom
              style={{ width: "100%", height: "100%" }}
            >
              <MapCenterSync center={center} zoom={zoom} />
              <TileLayerAny
                attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
              />
              {markers.map((marker, index) => (
                <MarkerAny key={index} position={marker.position} icon={resolveMarkerIcon(marker.kind)}>
                  {marker.label && <Popup>{marker.label}</Popup>}
                </MarkerAny>
              ))}
            </MapContainerAny>
          </div>
        );
      };

      setReady(true);
    }

    if (typeof window !== "undefined") {
      void load();
    }
  }, []);

  if (!isClient || !ready || !LeafletMapImpl) {
    return (
      <div className={className ?? "map-wrapper bg-gray-800 rounded-lg"}>
        <div className="flex h-full items-center justify-center text-gray-400">
          Loading map...
        </div>
      </div>
    );
  }

  const Impl = LeafletMapImpl;
  return <Impl center={center} zoom={zoom} markers={markers} className={className} />;
}
