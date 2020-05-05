# TL-backend
Backend do strony timeline.

## Api
Zrobic osobne wiki!
- Users
  - new user
  - delete user
- Timelines
  - new timeline
  - delete timeline
  - get timeline by username - pobiera glowna os usera
- Events
  - new event
  - delete event
  - get events by timeline id - pobiera eventy w danym timeline

## Opis
Pliki:
- User.java:
  - @Data robi getery setery - lombok,
  - @NoArgsConstruktor - tworzy konstruktor bez argumentow,
  - @Document - okresla tabele(dokumenty) w mongo.
- DataStartupService.java:
  - plik tworzy przykladowe dane przy uruchomieniu serwera - @EventListener
  - przy robieniu entities np usera to nie trzeba podawac id, stworzy sie samo
- SecurityConfig.java:
  - tez wazne przy uruchomieniu bo np mialem problem z uprawnieniami do DELETE
  - uwaga, mam ustawione ze kazdy ma dostep do wszystkiego!
  
  
## Uwagi
Mozna dodac:
- indeksowanie do np username - usprawnia wykonywanie queries, uwaga trzeba cos zmieniec w config mongo,
- pattern do emaila - zeby odrzucal zle emaile

Uruchamianie:
- uwaga w entities robic @Collection, a nie @Collation!!!
- uruchamienie mongo <code>sudo systemctl start mongod</code>
- przy pierwszym uruchomieniu mongo trzeba stworzyc nowego usera admin

Skladnia:
- @Valid - sprawdzanie czy wprowadzane dane sa odpowiednie do np. doadania do bazy,
- @Autowired - wstrzykiwanie obiektow