# OBD Analyzer

Zadaniem aplikacji jest pobieranie danych z samochodu przy pomocy OBD. Aplikacja loguje dane do pliku. Do komunikacji z modułem używa standardu Bluetooth.

## Komunikacja Bluetooth

W celu komunikacji z urządzeniem należy włączyć w smartfonie moduł Bluetooth. Można to zrobić przy użyciu przycisku, który znajduje się w aplikacji. 

![Bluetooth](img/bluetooth.PNG?raw=true "Bluetooth")

## Urządzenia OBD

Gdy moduł Bluetooth jest już włączony, widzimy listę urządzeń OBD znajdujących się w pobliżu. W celu połączenia należy wybrać konkretne urządzenie. Wcześniej należy je sparować z naszym telefonem. 

![Devices](img/devices.PNG?raw=true "Devices")

Urządzenie łączy się z OBD z wykorzyastaniem code setu dla kotrolera ELM327. Lista kodów służących do komunikacji dostępna jest na stronie https://en.wikipedia.org/wiki/Hayes_command_set. Nawiązanie połączenia przebiega według następującego schematu:
* wysłanie komendy 'Z0' (RESTART)
* ustawienie parametrów połączenia, komendy: 
  * 'D' (SET_ALL_DEFAULTS)
  * 'E0' (ECHO_OFF)
  * 'S0' (SPACES_OFF)
* Następnie do urządzenia wysyłane są cyklicznie zapytania o aktualne parametry działania pojazdu elem ich późniejszego wyświetlenia:
  * '04' (ENGINE_LOAD)
  * '0C' (ENGINE_RPM)
  * '0D' (VEHICLE_SPEED)

Na każdą z komend orządzenie OBD odpowiada wysyłając na początku pakietu ECHO wysłanego do niego polecenia, a dopiero później treść odpowiedzi z oznaczeniem polecenia, dla którego jest to odpowiedź, zakończoną znakiem '>'. Poszczególne części odpowiedzi oddzielone są kodami końca linii.

Przykład działania:
Polecenie ENGINE_RPM: [0, 1, 0, C]
Odpowiedź OBD: [0, 1, 0, C, , 4, 1, 0, C, 0, 0, 0, 0, , , >]
* Echo polecenia: [0, 1, 0, C].
* Oznaczenie na które pytanie jest wysyłana odpowiedź: [4, 1, 0, C].
* Parametry odpowiedzi (aktualna liczba obrotów silnika): [0, 0, 0, 0].

## Działanie aplikacji

Aplikacja wyświetla na ekranie zmierzone parametry oraz loguje je do pliku. Użytkownik dodatkowo widzi czy powinien włączyć wyższy czy niższy bieg w celu zmniejszenia zużycia paliwa. Jest to tylko PoC, ponieważ w realnym zastosowaniu nie powinno się polegać na tym, że kierowca będzie śledził komunikaty na ekranie telefonu. Można to jednak w prosty sposób zintegrować w przyszłości z komunikatami głosowymi.

![App](img/app.PNG?raw=true "App")

Przyciski LOG 1, 2, 3 służą dodatkowo do logowania informacji o ich wciśnięciu w danym momencie. Przyciski LOG mogą zostać użyte do tworzenia zakładek w pliku z zebranymi danymi z OBD w celu oznaczenia interesujących momentów jazdy.

Działanie aplikacji można zobaczyć na zamieszczonym filmiku: https://www.youtube.com/watch?v=nyZY-t37okM
