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
 1). Daca exista o regiune care nu poate fi aparata suficient, o determinam pe cea mai 
vulnerabila si punem restul armatelor (daca mai sunt) acolo (motivul: in caz ca inamicul 
ne va cuceri regiunea respectiva, va avea mai putine armate cu care sa atace regiunile vecine).

In functia care face transferurile si atacurile, mai intai transferam
toate armate de pe regiunile interioare (regiuni inconjurate numai de aliati) in
cea mai apropiata regiune care are vecini inamici sau neutrii. 
Apoi atacam din teritoriile de pe margine (care nu au numai aliati in jur) teritoriile, 
indiferent daca sunt inamici sau neutrii, care fac parte din cele mai pretioase superRegiuni 
pana cand nu mai avem cu ce sa atacam sau ramanem cu un numar de armate necesare apararii.

Partea random de la deploy este o modificare a codului original de pe site.
Scheletul de cod este de asemenea de pe site-ul pub.theaigames.com, la care noi am adaugat 
ce ne trebuie.

Etapa 2:

Update-uri: 
-prioritatea superregiunii este acum determinata de un raport intre bonus-ul acordat si
nr. total de armate ale subregiunilor sale

-in loc sa adaugam noi superregiuni in lista celor pe care vrem sa le capturam, acum aceasta
lista este recalculata in fiecare runda, pentru a nu fi limitat doar la cele planuite la 
inceput(pana cand le capturam complet) si pentru a ne extinde in orice superregiune pe care 
ne aflam sau cu care suntem vecini. Recalcularea ajuta la prioritizarea superRegiunilor
in functie de situatia din runda curenta

-in functia getStartingRegion nu mai adaugam superRegiunile alese in lista 
"superRegionsToConquer" pentru ca aceasta lista este calculata la inceputul fiecarei runde
din nou

-am adaugat o lista care retine ce regiuni de pe margine sunt in pericol(au cel putin un 
inamic ca vecin) si am transmis aceasta lista functiei de defend, astfel optimizand
algoritmul de aparare (vom stii direct ce regiuni trebuie aparate, in loc sa iteram prin toate
regiunile de pe margine)

-am inceput implementarea solutiei pentru o problema in care 2 regiuni diferite atacau acelasi 
vecin neutru; dorim sa folosim lista neutralTargetRegions pentru a stii daca deja am atacat
o regiune neutra pentru a nu o ataca din nou

-am optimizat partea "random" de la sfarsit-ul functiei care pune armate pe regiuni astfel incat
 sa se faca un "pooling" - acum pune toate armatele ramase pe o singura regiune, in loc 
sa le distribuie probabilistic egal, astfel crescand sansele ca o regiune sa aiba mai multe 
armate acumulate, ca in viitor sa invinga mai usor inamicii

-am rezolvat mici buguri

Etapa 3:

-am adaugat in clasa Region functia public int getUnfriendlyNeighbors(String me), care
intoarce numarul de regiuni "neprietenoase"(ale inamicului sau neutre) care sunt vecini cu regiunea
care apealeaza functia

-am rezolvat bugul prin care aveam regiuni care atunci cand atacau, foloseau si armatele desemenate
pentru aparare(care trebuiau sa ramana pe acel teritoriu) - acest bug era provocat de partea din 
functia getAttackTransferMoves in care tratam un caz : daca in jurul unei regiuni exista o singura 
regiune inamica, o atacam cu toate armatele pe care le avem pe acel teritoriu (daca avem sansa de a
o cuceri), stiind ca nu mai trebuie sa ne aparam de alti inamici. Bugul era cauzat de conditiile 
eronate care provocau atacul. De aceea am creat functia mentionata mai sus, getunfriendlyNeighbors.

In cazul in care acea functie intoarce 1, lista de inamici pe care ii putem ataca creata in functia
getAttackTransferMove are tot un inamic si acel inamic nu este neutru, daca va fi cucerita regiunea,
o vom ataca cu toate armatele. 

-am facut in asa fel incat o regiune neutra sa nu fie atacata de 2 ori/de 2 regiuni diferite, deoarece
regiunile neutre nu pot adauga intre timp armate(doar in cazul in care e cucerita, se schimba numarul 
de armate) si astfel odata ce o atacam avem garantia ca va fi luata aproape tot timpul(nu va fi luata
daca este cucerita de inamic si inamicul are mai multe armate decat poate invinge numarul de armate
cu care atacam); am folosit o lista neutralTargetRegions in care adaugam regiunile neutre pentru care
am decis ca le vom ataca - logica exista din etapa trecuta, doar ca era un bug in implementare
si de aceea am spus ca "am inceput implementarea" la etapa 2, pentru ca stiam ca nu merge.

-am rezolvat bugul prin care in lista attackTransferMovers adaugam un atac de la regiunea x la y de 
doua ori - era provocat de faptul ca noi adaugam din faza de deploy atacuri in lista, iar in faza
in care decideam atacurile, adaugam iar un atac de la x la y, uneori si cu un numar diferit de armate;
pentru a evita acest lucru, in clasa AttackTransferMove am suprascris functia equals(Object o) pentru 
a intoarce true doar daca regiunea fromRegion era aceeasi cu fromRegion din atacul cu care compar, 
si daca toRegion era aceeasi cu toRegion-ul celuilalt atac, numarul de armate fiind irelevant 
pentru ceea ce doream sa obtinem. Astfel, cand adaugam atacuri in lista de atacuri (attackTransferMoves),
verificam inainte daca acel atac exista deja, apeland contains(care foloseste equals) si daca exista
deja, scoatem acea miscare si o adaugam pe cea noua pentru ca cea noua are alte informatii
dupa care a luat decizia de a ataca (numarul de armate poate fi diferit).