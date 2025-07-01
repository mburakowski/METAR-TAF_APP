# METAR-TAF_decoder

**METAR-TAF_decoder** to aplikacja mobilna na Androida umożliwiająca pobieranie, dekodowanie oraz czytelną prezentację raportów lotniczych METAR/TAF na podstawie kodu ICAO wybranego lotniska.

## Funkcje

- **Logowanie i rejestracja użytkownika** (Firebase Auth)
- **Pobieranie i wyświetlanie danych METAR oraz TAF** na podstawie kodu ICAO
- **Dekodowanie i prezentacja czytelnej wersji raportów**
- **Historia wyszukiwań** – ostatnio wyszukiwane lotniska (Firestore + RecyclerView)
- **Ulubione lotniska** – lista bez duplikatów, możliwość dodania, usunięcia oraz szybkiego wypełniania pola ICAO (Firestore)
- **System powiadomień** – po pobraniu nowych raportów METAR/TAF
- **Odświeżanie wyników (Swipe to refresh)**
- **Wylogowywanie z aplikacji**
- Obsługa błędów (nieprawidłowy ICAO, brak danych, problemy z siecią)
- Przejrzysty interfejs oparty na klasycznym Androidzie (XML + Activity)

## Technologie

- **Kotlin** (Android)
- **Firebase Authentication** – obsługa logowania/rejestracji
- **Firebase Firestore** – ulubione lotniska, historia wyszukiwań
- **RecyclerView** – wyświetlanie historii i ulubionych
- **Powiadomienia Android** – systemowe powiadomienia
- **OkHttp + Gson** – pobieranie i parsowanie danych z API METAR/TAF
- **SwipeRefreshLayout** – wygodne odświeżanie danych

## Struktura projektu

- `HomeActivity` – główny ekran: wyszukiwanie, dekodowanie, ulubione, powiadomienia
- `LoginActivity` / `RegisterActivity` – logowanie/rejestracja użytkownika
- `HistoryActivity` – przeglądanie historii i ulubionych lotnisk (RecyclerView)
- Adaptery: `HistoryAdapter`, `FavoriteAdapter` – obsługa wyświetlania list
- Modele danych: klasy `MetarResponse`, `TafResponse` itd.

## Jak działa aplikacja?

1. Użytkownik loguje się lub rejestruje.
2. Wprowadza kod ICAO (np. `EPWA`) i pobiera raport METAR/TAF.
3. Może dodać lotnisko do ulubionych, kliknąć w ulubione aby automatycznie wypełnić pole ICAO, lub usunąć z ulubionych.
4. Przegląda historię wyszukiwań i ulubione lotniska.
5. Otrzymuje powiadomienia po pobraniu danych.
6. Może odświeżać dane lub się wylogować.


