package ru.practicum.ewmservice.models.location;

public interface CalculateDistance {
    default double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        if (lat1 == lat2 && lon1 == lon2) {
            return 0.0;
        } else {
            // переводим градусы широты в радианы
            double radLat1 = Math.PI * lat1 / 180;
            // переводим градусы долготы в радианы
            double radLat2 = Math.PI * lat2 / 180;
            // находим разность долгот
            double radTheta = lon1 - lon2;
            // находим длину ортодромии
            double dist = Math.sin(radLat1) * Math.sin(radLat2) +
                    Math.cos(radLat1) * Math.cos(radLat2) * Math.cos(radTheta);
            if (dist > 1) {
                dist = 1.0;
            }
            dist = Math.acos(dist);
            // переводим радианы в градусы
            dist = dist * 180 / Math.PI;
            // переводим градусы в километр
            dist = dist * 60 * 1.8524;
            return dist;
        }

    }
}
