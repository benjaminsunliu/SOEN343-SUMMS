-- Migration: Add CO₂ emissions tracking column to trips table
-- Date: 2026-04-07
-- Description: Adds co2_saved_kg column to store CO₂ emissions saved for each trip

ALTER TABLE trips ADD COLUMN co2_saved_kg DOUBLE DEFAULT 0.0 AFTER total_duration_minutes;

-- Index for efficient CO₂ queries
CREATE INDEX idx_trips_co2_citizen ON trips(citizen_id, co2_saved_kg);

