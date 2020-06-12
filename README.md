# TL-backend
Backend do strony timeline.

## elasticTest
Branch testowy dla elasticsearch. Elasticsearch to komplenty system lacznie z baza danych!!! umozliwiajacy bardzo zaawansowana analize danych. Wykorzystanie to np totalnie zrezygnowanie z mongo i przeniesienie danych do elasticsearch lub np polaczenie ze czesc danych wysylamy na elasticsearch i tam analizujemy. UWAGA entities musza byc osobne - nie tak jak jest teraz ze mam Timeline dla mongo i elasticsearch - to nie zadziala. Plus elasitcsearch musi byc uruchomiony np poprzez dockera.

## Wyszukiwarka
Ja zrobie to po prostu poprzez wbudowane indeksowanie tekstu w mongodb. Elasticsearch na pewno dziala lepiej ale musialbym wszystkie dane przeniesc. Plus uwaga springboot-data-elasticsearch dziala tylko z maven a nie gradle.
Elasticsearch to dobra opcja na przyszlosc gdy bede potrzebowal lepszego systemu wyszukiwania lub bedzie bardzo duzo uzytkownikow i bedzie trzeba analizowac dane.
