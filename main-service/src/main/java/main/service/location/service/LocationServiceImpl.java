package main.service.location.service;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import main.service.location.Location;
import main.service.location.LocationRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class LocationServiceImpl implements LocationService {
    LocationRepository locationRepository;

    @Override
    public Location save(Location location) {
        return locationRepository.save(location);
    }
}
