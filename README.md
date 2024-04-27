Głównym celem projektu jest utworzenie autonomicznego systemu drzwiczek pozwalającego na umożliwienie bądź uniemożliwienie wejścia do pomieszczenia przez zwierzę. System dokonuje tego - korzystając z kamery, na której obrazie uruchamiany jest algorytm sztucznej inteligencji wykrywający zwierzę z aktualnie skompilowanego modelu na serwerze. 
  
Komputerem urządzenia jest raspberry pi, które do poprawnego działania wymaga podłączenia zewnętrznego zasilania. Mile widzianym jest dostęp do Internetu, jednakże nie jest on wymagany po przeprowadzeniu wstępnej konfiguracji, tylko wtedy urządzenie umożliwia jedynie otwieranie i zamykanie drzwiczek. 
  
System w minimalnej konfiguracji po wykryciu dowolnego “wspieranego” zwierzęcia (czyli dowolny kot lub pies, na których bazie utworzony został model SI) otwiera drzwi, pozwalając na przejście, następnie po odczekaniu 5s po wyjściu zwierzęcia z pola kamery drzwi zostają zamknięte. W celach bezpieczeństwa komora drzwi wyposażona jest w czujnik zbliżeniowy wykrywający czy w środku nie znajduje się zwierzę (lub dowolny obiekt) - przerywając procedurę zamykania do momentu opuszczenia komory przez obiekt. 
  
Użytkownik do obsługi urządzenia wykorzystuje graficzny interfejs na telefonie z systemem android pozwalający na odblokowanie dodatkowych możliwości, które wymagają dostępu do Internetu. 
  
-	Galeria zwierząt przechowująca obrazki pupilów na serwerze pod postacią folderów - uruchomionym na raspberry pi (liczba zdjęć i zwierząt jest ograniczona dostępną pamięcią). Użytkownik na bazie tych zdjęć może wygenerować model sztucznej inteligencji w celu umożliwienia dostępu do pomieszczenia specyficznym zwierzętom (np. Dostęp do pożywienia tylko dla kotów). Do wykorzystania tej opcji wymagana jest duża ilość zdjęć dodanych zwierzaków w różnej konfiguracji, a poprawność działania zależy tylko od zdjęć użytkownika. Należy pamiętać, że proces ten jest długotrwały i przy dużej liczbie zdjęć może potrwać nawet dłużej niż godzinę. 
  
-	Dostrajanie systemu, gdzie możemy na stałe zablokować lub odblokować drzwi, w tym też stanie kamera drzwiczek jest wyłączona, lub wymusić otwarcie i zamknięcie (z poszanowaniem bezpieczeństwa zwierzęcia). Możliwe jest także zmiana czasu, po którym drzwiczki się zamykają z przedziału od 1 do 10 sekund, gdyby zwierzę miało problem z przejściem. 
  
-	Widok z kamery, który jest wysyłany użytkownikowi na telefon wraz z widokiem wykrytego zwierzęcia. Aplikacja działa, nawet jeżeli telefon użytkownika nie jest podłączony do sieci lokalnej - pozwalając na sprawdzenie stanu obserwowanego pokoju. Użytkownik posiada też możliwość zwiększenia częstotliwości, z jaką każde zdjęcie jest analizowane przez SI z przedziału od 3 do 15s. 


Zrzuty ekranu aplikacji

![alt text](image.png)
![alt text](image-1.png)
![alt text](image-2.png)