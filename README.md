# Реализация передачи координат местоположения транспортного средства согласно протоколу 285.

Передача данных с использованием транспортного протокола, определенного в приказе Минтранса РФ от 31 июля 2012 г. № 285 
"Об утверждении требований к средствам навигации, функционирующим с использованием навигационных сигналов 
системы ГЛОНАСС или ГЛОНАСС/GPS и предназначенным для обязательного оснащения транспортных средств категории М, 
используемых для коммерческих перевозок пассажиров, и категории N, используемых для перевозки опасных грузов"

```java
	// implement interface
	public class Coordinate implements TrackingCoordinate {
		
	}
```

```java
	// create sender
	senderRunnable = new SenderRunnable(host, port, SOCKET_TIMEOUT, new Sender()) {
		@Override
		protected List<? extends TrackingCoordinate> getCoordinates() {
			// return coordinates from database or other storage
			return dao.queryBuilder()
					.limit(PACKET_MAX_SIZE)
					.list();
		}
	
		@Override
		protected void onSuccess(List<? extends TrackingCoordinate> transmitted) {
			// delete transmitted coordinates from storage if need
			dao.deleteInTx(daoCoordinates);
		}
	};
```

```java
	// save your coordinates in the store
	@Override
	public void onLocationChanged(Location location) {
		// I use SQLite database and awesome greenDAO for storing coordinates
		dao.insert(Coordinate.buildFromLocation(tracker, location));
	}	
```

```java
	// listen location changes and start sender
	public void start() {
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, this);
	
		scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
		scheduledExecutor.scheduleWithFixedDelay(senderRunnable, 0, sendInterval, TimeUnit.MILLISECONDS);
	}
```