Belmega Diana-Elena, 322CA
Bojinovici Andrei-Lucian, 322CA
Patrascanu Andra-Maria, 322CA
Vicol Anca-Diana, 322CA

Etapa 1:

Strategia noastra trateaza doua cazuri: 

1)N-avem inamici vizibili:
Determinam superRegiunea cu cel mai mare raport bonus/efort(nr. de 
regiuni de cucerit) si o prioritizam.
Acolo vom pune armate pentru a o cuceri. Se repeta pana cand nu mai avem armate.
Nota: superRegiunile cu aceeasi prioritate, dar care necesita mai putin efort
(mai putine regiuni de cucerit) sunt prioritare.
In timpul deploy-ului, cream o lista cu atacurile viitoare (pentru a nu mai parcurge
din nou aceleasi for-uri).

2) Daca avem inamici vizibili:
Ne aparam regiunile vecine cu inamicii, in functie de cate armate au momentan (fara a 
prezice ca vor fi mai multe). Daca toate sunt aparate, se continua cu strategia de la punctul
 1). Daca exista o regiune care nu poate fi aparata suficient, o determinam pe cea mai vulnerabila si punem restul armatelor (daca mai sunt) acolo (motivul: in caz ca inamicul ne va cuceri regiunea respectiva, va avea mai putine armate cu care sa atace regiunile vecine).

In functia care face transferurile si atacurile, mai intai transferam
toate armate de pe regiunile interioare (regiuni inconjurate numai de aliati) in
cea mai apropiata regiune care are vecini inamici sau neutrii. 
Apoi atacam din teritoriile de pe margine (care nu au numai aliati in jur) teritoriile, indiferent daca sunt inamici sau neutrii, care fac parte din cele mai pretioase superRegiuni pana cand nu mai avem cu ce sa atacam sau ramanem cu un numar de armate necesare apararii.

Partea random de la deploy este o modificare a codului original de pe site.
Scheletul de cod este de asemenea de pe site-ul pub.theaigames.com, la care noi am adaugat ce ne trebuie.

Etapa 2:

Update-uri: 
-prioritatea superregiunii este acum determinata de un raport intre bonus-ul acordat si
nr. total de armate ale subregiunilor sale

-in loc sa adaugam noi superregiuni in lista celor pe care vrem sa le capturam, acum aceasta
lista este recalculata in fiecare runda, pentru a nu fi limitat doar la cele planuite la 
inceput(pana cand le capturam complet) si pentru a ne extinde in orice superregiune pe care 
ne aflam sau cu care suntem vecini. Recalcularea ajuta la prioritizarea superRegiunilor
in functie de situatia din runda curenta

-in functia getStartingRegion nu mai adaugam superRegiunile alese in lista "superRegionsToConquer" pentru ca aceasta lista este calculata la inceputul fiecarei runde
din nou

-am adaugat o lista care retine ce regiuni de pe margine sunt in pericol(au cel putin un 
inamic ca vecin) si am transmis aceasta lista functiei de defend, astfel optimizand
algoritmul de aparare (vom stii direct ce regiuni trebuie aparate, in loc sa iteram prin toate
regiunile de pe margine)

-am inceput implementarea solutiei pentru o problema in care 2 regiuni diferite atacau acelasi vecin neutru; dorim sa folosim lista neutralTargetRegions pentru a stii daca deja am atacat
o regiune neutra pentru a nu o ataca din nou

-am optimizat partea "random" de la sfarsit-ul functiei care pune armate pe regiuni astfel incat sa se faca un "pooling" - acum pune toate armatele ramase pe o singura regiune, in loc 
sa le distribuie probabilistic egal, astfel crescand sansele ca o regiune sa aiba mai multe armate acumulate, ca in viitor sa invinga mai usor inamicii

-am rezolvat mici buguri