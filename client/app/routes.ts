import { type RouteConfig, index, route } from "@react-router/dev/routes";

export default [
	index("routes/login.tsx"),
	route("register", "routes/register.tsx"),
	route("", "routes/protected-layout.tsx", [
		route("dashboard", "routes/dashboard.tsx"),
		route("vehicles/search", "routes/vehicle-search.tsx"),
		route("reservation", "routes/reservation.tsx"),
		route("my-reservations", "routes/my-reservations.tsx"),
		route("payment", "routes/payment.tsx"),
		route("vehicle-return", "routes/vehicle-return.tsx"),
		route("provider/vehicles", "routes/provider-vehicles.tsx"),
		route("provider/operations", "routes/provider-operations.tsx"),
		route("services/parking", "routes/parking.tsx"),
		route("services/public-transport", "routes/public-transport.tsx"),
		route("analytics/rentals", "routes/analytics-rentals.tsx"),
		route("analytics/gateway", "routes/analytics-gateway.tsx"),
	]),
] satisfies RouteConfig;
