import { useEffect, useState, type ComponentType } from "react";

// Local alias for coordinates instead of importing from Leaflet types
type LatLngExpression = [number, number] | { lat: number; lng: number };

export interface MapMarker {
  position: LatLngExpression;
  label?: string;
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
      const L = await import("leaflet");
      await import("leaflet/dist/leaflet.css");

      const markerIcon2x = (await import("leaflet/dist/images/marker-icon-2x.png")).default as string;
      const markerIcon = (await import("leaflet/dist/images/marker-icon.png")).default as string;
      const markerShadow = (await import("leaflet/dist/images/marker-shadow.png")).default as string;

      // Fix default icon paths so markers render correctly after bundling
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      delete (L.Icon.Default.prototype as any)._getIconUrl;
      L.Icon.Default.mergeOptions({
        iconRetinaUrl: markerIcon2x,
        iconUrl: markerIcon,
        shadowUrl: markerShadow,
      });

      const reactLeaflet = await import("react-leaflet");
      const { MapContainer, Marker, Popup, TileLayer } = reactLeaflet;

      LeafletMapImpl = function LeafletMapInner(props: LeafletMapProps) {
        const { center, zoom = 13, markers = [], className } = props;

        return (
          <div className={className ?? "map-wrapper"}>
            <MapContainer
              center={center}
              zoom={zoom}
              scrollWheelZoom
              style={{ width: "100%", height: "100%" }}
            >
              <TileLayer
                attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
              />
              {markers.map((marker, index) => (
                <Marker key={index} position={marker.position}>
                  {marker.label && <Popup>{marker.label}</Popup>}
                </Marker>
              ))}
            </MapContainer>
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

