package nextstep.subway.domain;

import javax.persistence.Embeddable;
import javax.persistence.EntityExistsException;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Embeddable
public class LineStations {
    @OneToMany(mappedBy = "line", orphanRemoval = true)
    private final List<LineStation> lineStations = new ArrayList<>();

    protected LineStations() {

    }

    public LineStations(List<LineStation> lineStations) {
        validation(lineStations);
        this.lineStations.addAll(lineStations);
    }

    public void addLineStation(final LineStation lineStation) {
        Optional<LineStation> isLineStation = validation(lineStation);
        isLineStation.ifPresent(station -> station.updateLineStation(lineStation));
        this.lineStations.add(lineStation);
    }

    public boolean isContains(final LineStation lineStation) {
        return this.lineStations.contains(lineStation);
    }

    public LineStations getLineStationBySorted() {
        List<LineStation> result = new ArrayList<>();
        LineStation startStation = findStartStation();
        result.add(startStation);
        insertLineStationBySorted(result, startStation);
        return new LineStations(result);
    }

    private void insertLineStationBySorted(List<LineStation> result, LineStation startStation) {
        Optional<LineStation> preStation = findPreStation(startStation.getCurrentStation());
        while (preStation.isPresent()) {
            LineStation station = preStation.get();
            result.add(station);
            preStation = findPreStation(station.getCurrentStation());
        }
    }

    private Optional<LineStation> validation(final LineStation lineStation) {
        if (this.lineStations.isEmpty()) {
            return Optional.empty();
        }
        Optional<LineStation> isPreStation = findCurrentStation(lineStation.getPreStation());
        Optional<LineStation> isCurrentStation = findCurrentStation(lineStation.getCurrentStation());
        if (Objects.equals(isPreStation.isPresent(), isCurrentStation.isPresent())) {
            throw new IllegalArgumentException("invalid Argument");
        }
        return this.lineStations.stream().filter(item -> item.isAddLineStation(lineStation)).findAny();
    }

    private LineStation findStartStation() {
        return this.lineStations.stream().filter(LineStation::isStartStation).findFirst().orElseThrow(EntityExistsException::new);
    }

    private Optional<LineStation> findPreStation(final Station station) {
        return this.lineStations.stream()
                .filter(savedLineStation -> savedLineStation.isPreStation(station))
                .filter(savedLineStation -> !savedLineStation.isCurrentStation(station))
                .findFirst();
    }

    private Optional<LineStation> findCurrentStation(final Station station) {
        return this.lineStations.stream()
                .filter(savedLineStation -> savedLineStation.isCurrentStation(station))
                .findFirst();
    }

    private void validation(List<LineStation> lineStations) {
        if (Objects.isNull(lineStations)) {
            throw new IllegalArgumentException("invalid argument");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LineStations that = (LineStations) o;
        return Objects.equals(lineStations, that.lineStations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lineStations);
    }
}
