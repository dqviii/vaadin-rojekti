# Vaadin Web -harjoitustyö (elokuvateema)

Vaadin harjoitustyö, joka toteuttaa enemmän tai vähemmän järkevän elokuvateemaisen "tietokantasovelluksen".

Harjoitustyö toteuttaa omien laskujen mukaan 34/39 vaatimusta.

---

## Sovelluksen ajaminen Dockerilla (PostgreSQL kanta + sovellus)

Vaatimukset: Docker Desktop (Windows/macOS) tai docker + docker compose -plugin (Linux), Git.

1. **Kloonaa repo** paikallisesti.
   ```bash
   git clone https://github.com/dqviii/vaadin-rojekti.git
   cd vaadin-rojekti
   ```

2. **Käynnistä pino.** Ensimmäinen build voi kestää 5–10 min (Maven + Vaadin-bundle).
   ```bash
   docker compose up --build
   ```

3. **Avaa selain** [http://localhost:8080](http://localhost:8080), kun lokissa lukee `Started Application in N seconds`.

4. **Kirjaudu seedatulla tilillä.** Käynnistys seedaa kolme käyttäjää (idempotentisti):
   - `Admin / admin123` – ADMIN-rooli
   - `Super / super123` – SUPER-rooli
   - `User  / user123`  – USER-rooli

   Lisäksi seedataan 10 genreä, 10 elokuvaa genreliitoksin ja jokaiselle elokuvalle `MovieStats`-rivi. Sekä myös 3 esitysaikaa (Showtime) elokuville.

5. **Pysäytä pino** `Ctrl+C` tai `docker compose down`. Postgresin `db-data`-volume säilyy, joten käyttäjän luomat tiedot ovat seuraavalla ajokerralla tallessa. Täysi nollaus seedereiden uusintaa varten: `docker compose down -v`.

---

## Vaatimusseuranta

### A. Data, entiteetit ja CRUD

#### A1. Yksi entiteetti
- Tila: [x]
- Todiste: `Movie.java`, `MovieRepository.java`, `MovieService.java`, `MovieListView.java` (reitti `/movies`).
- Miten toteutettu: `Movie`-entiteetti (title, director, releaseDate, ageRating, originalLanguage) + JPA-repo + `@Transactional`-palvelu hoitavat luonnin, päivityksen, listauksen ja poiston. `MovieListView`ssa on lazy-latautuva Grid, dialogi `BeanValidationBinder`illa ja `ConfirmDialog`-poisto. UI-toiminnot päätyvät tietokantaan asti.

#### A2. Toinen entiteetti + 1:1, relaatio näkyy listauksessa
- Tila: [x]
- Todiste: `MovieStats.java`, `MovieStatsService.java`, `MovieStatsListView.java` (reitti `/movie-stats`); `MovieRepository#findMoviesWithoutStats`.
- Miten toteutettu: `MovieStats` viittaa `Movie`hin `@OneToOne` + `@JoinColumn(name="movie_id", unique=true, nullable=false)` -kentällä, joten yhdellä elokuvalla voi olla enintään yksi tilastorivi. Editorin ComboBoxiin tarjotaan vain `moviesWithoutStats()`-tulos, jotta duplikaattia ei pääse syntymään. **Relaatio näkyy** Gridin ensimmäisessä "Movie"-sarakkeessa (`stats.getMovie().getTitle()`); muut sarakkeet ovat tilastoja.

#### A3. Kolmas entiteetti + 1:N, relaatio näkyy listauksessa
- Tila: [x]
- Todiste: `Showtime.java`, `ScreenType.java`, `ShowtimeService.java`, `ShowtimeListView.java` (reitti `/showtimes`).
- Miten toteutettu: `Showtime` on relaation "many"-pää: `@ManyToOne` + `@JoinColumn(name="movie_id", nullable=false)`. Yhdellä elokuvalla voi olla monta esitysaikaa, mutta jokainen Showtime kuuluu yhdelle elokuvalle. `ScreenType` (STANDARD_2D, IMAX, IMAX_3D, DOLBY_ATMOS, FOUR_DX) tallennetaan stringinä. Lista järjestetään oletuksena `startTime DESC`, ja syötteenä on `DateTimePicker` 5 minuutin tarkkuudella. **Relaatio näkyy** "Movie"-sarakkeessa.

#### A4. Neljäs entiteetti + M:N, relaatio näkyy listauksessa
- Tila: [x]
- Todiste: `Genre.java`, `GenreService.java`, `GenreListView.java` (reitti `/genres`); `Movie.java` (`Set<Genre> genres` + `@JoinTable("movie_genre")`); `MovieListView` (Genres-sarake + `MultiSelectComboBox<Genre>`).
- Miten toteutettu: M:N-relaatio omistetaan `Movie`-puolelta: `@ManyToMany(fetch=EAGER)` + `@JoinTable("movie_genre")`. `Genre` on yksisuuntainen (ei takareferenssiä). Genren poistossa ajetaan ensin natiivi `deleteGenreLinks`, joka tyhjentää liitostaulun rivit kyseiselle genrelle, jotta foreign key ei kaadu. **Relaatio näkyy** `MovieListView`n "Genres"-sarakkeessa pilkulla erotettuna; editorin `MultiSelectComboBox` sitoo valinnan `LinkedHashSet`iin molempiin suuntiin.

#### A5. Jokaisella entiteetillä vähintään 5 validoitavaa kenttää
- Tila: [x]
- Todiste: `Movie.java` (5), `MovieStats.java` (6), `Showtime.java` (6), `Genre.java` (5).
- Miten toteutettu: Vaatimus tulkitaan koskemaan A1–A4-domain-entiteettejä. `User` on autentikoinnin infraa (Spring Securityn UserDetails-malli) eikä osa elokuva-domeenia, joten sitä ei lasketa.

  - **Movie**: `title` `@NotBlank @Size(max=200)`, `directorName` `@NotBlank @Size(max=120)`, `releaseDate` `@NotNull @PastOrPresent`, `ageRating` `@NotNull`, `originalLanguage` `@NotBlank @Size(max=40)`.
  - **MovieStats**: `movie` `@NotNull`, `budget` `@NotNull @PositiveOrZero`, `boxOfficeRevenue` `@NotNull @PositiveOrZero`, `runtimeMinutes` `@NotNull @Positive @Max(600)`, `imdbRating` `@NotNull @DecimalMin("0.0") @DecimalMax("10.0")`, `reviewCount` `@NotNull @PositiveOrZero`.
  - **Showtime**: `movie` `@NotNull`, `startTime` `@NotNull`, `theaterHall` `@NotBlank @Size(max=50)`, `screenType` `@NotNull`, `ticketPrice` `@NotNull @PositiveOrZero`, `availableSeats` `@NotNull @PositiveOrZero @Max(2000)`.
  - **Genre**: `genreName` `@NotBlank @Size(max=60)` (+ `unique=true`), `description` `@NotBlank @Size(max=5000)` (`columnDefinition = "TEXT"`, koska kentässä on Quillin tuottama HTML), `iconCode` `@NotBlank @Size(max=40)`, `mainstream` `@NotNull`, `targetAudience` `@NotNull`.

  Validointi tarkistetaan kahdesti: `BeanValidationBinder` näyttää kenttäkohtaiset virheet ennen tallennusta, ja Hibernate hylkää rikkovat insertit/updatet myös palvelinpäässä.

---

### B. Suodattaminen (Criteria API)

#### B1. Suodatus vähintään 3 syötekentällä
- Tila: [x]
- Todiste: `MovieSearchCriteria.java`, `MovieSearchService.java`, `MovieSearchView.java` (reitti `/movies/search`).
- Miten toteutettu: `MovieSearchService` rakentaa dynaamisen `CriteriaQuery<Movie>`-haun: predikaatti lisätään vain niille kentille, joihin käyttäjä on antanut arvon. Hakulomakkeessa on kuusi itsenäistä syötettä (pikahaku, ikäraja, julkaisupäivän alku/loppu, genre, genren nimi), joten kolmen kentän minimi täyttyy reilusti.

#### B2. Päivämäärävälin suodatus
- Tila: [x]
- Todiste: `MovieSearchCriteria.java` (`releaseDateFrom`, `releaseDateTo`); `MovieSearchService.java` (`cb.between` / `cb.greaterThanOrEqualTo` / `cb.lessThanOrEqualTo`); `MovieSearchView` (Released after / before -DatePickerit).
- Miten toteutettu: Molemmat rajat ovat valinnaisia ja käsitellään kolmessa haarassa: jos molemmat on annettu `cb.between`, jos vain alaraja `cb.greaterThanOrEqualTo`, jos vain yläraja `cb.lessThanOrEqualTo`. Tulos AND-yhdistetään muihin suodattimiin.

#### B3. Relaatioon perustuva suodatus JOINilla
- Tila: [x]
- Todiste: `MovieSearchCriteria.java` (`genre`); `MovieSearchService.java` (`Join<Movie, Genre> genreJoin = movie.join("genres")` + `cb.equal(genreJoin.get("id"), ...)` + `cq.distinct(true)`); `MovieSearchView` (`ComboBox<Genre>`).
- Miten toteutettu: Criteria-tason JOIN `Movie`-juuresta `genres`-relaatioon kääntyy Hibernatessa M:N-liitostaulun (`movie_genre`) kautta `Genre`-tauluun, ja predikaatti `cb.equal(genreJoin.get("id"), criteria.genre().getId())` rajaa elokuvat valitun genren id:n perusteella. M:N-liitos voi tuottaa duplikaattirivit, joten `cq.distinct(true)` asetetaan aina kun JOINia käytetään.

#### B4. Suodatus relaatioentiteetin ominaisuuden perusteella
- Tila: [x]
- Todiste: `MovieSearchCriteria.java` (`genreNameContains`); `MovieSearchService.java` (`cb.like(cb.lower(genreJoin.get("genreName")), ...)`); `MovieSearchView` ("Genre name contains" -TextField).
- Miten toteutettu: Sama `genreJoin` kuin B3:ssa käytetään myös liitetyn entiteetin **ominaisuuden** suodatukseen — case-insensitive osatekstihaku `genreName`-kenttään (`cb.like` + `cb.lower`). Jos sekä B3:n yhtäläisyysvertailu että B4:n osatekstihaku ovat aktiivisia samaan aikaan, sama JOIN jaetaan, jolloin saman genren on toteutettava molemmat ehdot.

#### B5. Monimutkainen haku, esim. `(X OR Y) AND Z`
- Tila: [x]
- Todiste: `MovieSearchCriteria.java` (`searchText`); `MovieSearchService.java` (`cb.or(title LIKE, directorName LIKE)`); `MovieSearchView` ("Quick search (title or director)").
- Miten toteutettu: Aiemmat erilliset Title- ja Director-kentät yhdistettiin yhdeksi `searchText`-pikahauksi, joka muodostaa OR-osan: `cb.or(title LIKE :q, directorName LIKE :q)`. Kun pikahakuun yhdistetään muita suodattimia, lopullinen WHERE on muotoa `(title LIKE :q OR directorName LIKE :q) AND ageRating = :r AND releaseDate BETWEEN :from AND :to AND genre = :g AND genreName LIKE :gn` — eli `(X OR Y) AND Z` luonnollisesti laajennettuna.

---

### C. Tyylit ja ulkoasu

#### C1. Globaalit tyylimuutokset (fontti, paletti, oletustyyli)
- Tila: [x]
- Todiste: `src/main/frontend/themes/movie/styles.css`, `themes/movie/theme.json`, `Application.java` (`@Theme("movie")`).
- Miten toteutettu: Käytössä on oma sovellusteema "movie". Google Fonts tarjoaa runkofontiksi `Lora`n ja otsikkofontiksi `Playfair Display`n. Useita `--lumo-*`-väritokeneita on ylikirjoitettu (mm. `--lumo-primary-color`, `--lumo-base-color`, `--lumo-body-text-color`, `--lumo-header-text-color`), reunan pyöristykset on terävöitetty ja varjot syvennetty — yhteisilme on retrosävyinen elokuvateatteri, ei Lumon oletuspaletti.

#### C2. Komponenttityylit: `addClassName`, `getStyle().set`, `addThemeVariants`
- Tila: [x]
- Todiste: `MovieListView.java` (`addClassName("movie-list-view")`, `toolbar.getStyle().set(...)`, `newMovieBtn.addThemeVariants(ButtonVariant.PRIMARY)`); `themes/movie/styles.css` (`.movie-list-view`).
- Miten toteutettu: `MovieListView` käyttää kaikkia kolmea rajapintaa näkyvästi: `addClassName` antaa juurinäkymälle CSS-luokan jolle teemassa on oma tausta, `getStyle().set` asettaa toolbarille primary-värisen alarajan ja paddingin, ja `addThemeVariants(ButtonVariant.PRIMARY)` korostaa "New movie"-painikkeen.

#### C3. Näkymäkohtainen CSS, joka vaikuttaa vain yhteen näkymään
- Tila: [x]
- Todiste: `src/main/frontend/styles/access-denied.css` (kaikki säännöt juuriluokan `.access-denied-view` alla); `AccessDeniedView.java` (luokkatason `@CssImport("./styles/access-denied.css")` + `addClassName("access-denied-view")` konstruktorissa).
- Miten toteutettu: Tyylitiedosto sijaitsee `src/main/frontend/styles/`-kansiossa (ei globaalissa `themes/movie/`-kansiossa) ja ladataan luokkatason `@CssImport`-annotaatiolla. **Skooppaus toimii nimitilana**: jokainen selektori alkaa juuriluokalla `.access-denied-view` (esim. `.access-denied-view .access-denied-card`, `.access-denied-view .access-denied-cta:hover`), joten säännöt eivät vuoda muihin näkymiin.

#### C4. Vähintään 5 Lumo/Aura utility -kategoriaa
- Tila: [x]
- Todiste: `MemberLoungeView.java` — kuusi `LumoUtility`-luokkaa kuudesta eri kategoriasta `addClassNames(...)`-kutsulla.
- Miten toteutettu: Konstruktorissa lisätään `LumoUtility.Padding.LARGE`, `Margin.Top.MEDIUM`, `Gap.MEDIUM` ja `AlignItems.CENTER`; otsikolle erikseen `FontSize.XXLARGE` ja paragrafille `TextColor.SECONDARY`. Kategorioita on siis kuusi (Padding, Margin, Gap, AlignItems, FontSize, TextColor) — vaadittu viisi täyttyy reilusti. Utility-tyylit ladataan globaalisti `Application`-luokassa annotaatiolla `@StyleSheet(Lumo.UTILITY_STYLESHEET)`, joten erillistä CSS-importtia ei tarvita.

#### C5. CSS: hover + focus + transition
- Tila: [x]
- Todiste: `access-denied.css` (`.access-denied-cta` jolla on `transition`, `:hover` ja `:focus-visible`); `AccessDeniedView.java` (`goHome.addClassName("access-denied-cta")`).
- Miten toteutettu: Yksi luokka `.access-denied-cta` (Back to home -painike) toteuttaa kaikki kolme: `transition` 180ms easella kolmelle propertylle (`transform`, `box-shadow`, `background-color`), `:hover` nostaa painikkeen kaksi pikseliä ylös ja tummentaa taustaa, ja `:focus-visible` lisää näppäimistöfokuksessa primary-värisen 2px outlinerenkaan. Tilamuutokset sulautuvat sujuvasti transitionin ansiosta.

---

### D. Ulkoasu ja rakenne (SPA)

#### D1. `MainLayout` + `@Route(value, layout = MainLayout.class)`
- Tila: [x]
- Todiste: `MainLayout.java` (`extends AppLayout`, `@Layout`); `HomeView.java` (`@Route(value = "", layout = MainLayout.class)` + `@AnonymousAllowed`).
- Miten toteutettu: HomeView reititetty juureen ja käyttää eksplisiittisesti `MainLayout`-luokkaa. MainLayout sisältää headerin, drawerin (SideNav) ja footerin. `@AnonymousAllowed` sallii etusivun julkisen näkemisen ennen kirjautumista.

#### D2. Vähintään 3 erilaista näkymälayoutia
- Tila: [x]
- Todiste: `HomeView.java` (dashboard); CRUD-listanäkymät `MovieListView`/`MovieStatsListView`/`ShowtimeListView`/`GenreListView`; `MovieSearchView.java` (split-haku); CSS `themes/movie/styles.css` osiot `.home-*` ja `.movie-search-*`.
- Miten toteutettu: Layouteja on kolme rakenteellisesti erilaista, kaikki saman `MainLayout`in päällä:
  - **(1) Dashboard** — `HomeView`ssa on hero-otsikko, neljä KPI-korttia (Movies / Showtimes / Genres / Stat profiles, lukemat tulevat palveluiden `count()`-metodeilta) ja "Recent showtimes"-lista (5 viimeisintä `findRecent(5)`-kutsulla). Sisältö on `Div`/`Span`-elementtejä CSS-gridissä — ei Vaadin-Gridiä — ja korteilla on hover-efekti.
  - **(2) Toolbar + Grid CRUD** — neljä listanäkymää jakavat saman pohjan: ylhäällä toolbar (otsikko + "New X"-painike), alla full-bleed Vaadin-Grid lazy-latauksella, dialog-pohjainen luonti/muokkaus ja `ConfirmDialog`-poisto.
  - **(3) Split-haku** — `MovieSearchView` käyttää `SplitLayout`ia: vasemmalla 28% leveä suodatinpaneeli parchment-taustalla, oikealla otsikko + täyskorkea Grid. Eroaa CRUD-pohjasta sillä, että sisältö on rinnakkain eikä pinottuna.

#### D3. Header: nimi/logo, käyttäjä, logout, DrawerToggle
- Tila: [x]
- Todiste: `MainLayout.java` (`addToNavbar(...)`, `createUserMenu`); `ViewTitle.java`; `themes/movie/styles.css` (`vaadin-app-layout::part(navbar)`, `.app-header`).
- Miten toteutettu: AppLayoutin navbarissa on `DrawerToggle`, logo + sovellusnimi ("Movie Database Application"), käyttäjätunnus (`AuthenticationContext.getPrincipalName()`) ja Logout-painike (`AuthenticationContext.logout()` — Spring Securityn oikea logout-flow CSRF-tokeneilla). Anonyymille näytetään Sign in -linkki. Teema-CSS antaa navbarille gradient-taustan, primary-värisen alarajan ja pehmeän varjon.

#### D4. Navigaatio: linkit, eri ikonit, aktiivisen sivun korostus
- Tila: [x]
- Todiste: `MainLayout.java` (`createSideNav()` rakentaa SideNavin `MenuConfiguration.getMenuEntries()`-listasta); `@Menu`-annotaatiot kuudessa näkymässä; aktiivisen tilan tyylit `themes/movie/styles.css` (`vaadin-side-nav-item::part(item)`, `:hover`, `[active]`).
- Miten toteutettu: Drawerin SideNav rakennetaan dynaamisesti `MenuConfiguration.getMenuEntries()`-listasta, joten kaikki `@Menu`-annotoidut näkymät tulevat automaattisesti navigaatioon. Jokaisella näkymällä on oma `VaadinIcon`: Home `vaadin:home`, Movies `vaadin:film`, Search movies `vaadin:search`, Movie stats `vaadin:chart`, Showtimes `vaadin:ticket`, Genres `vaadin:tags` — kuusi reittiä, kuusi eri ikonia. Aktiivinen reitti tunnistetaan Vaadinin omalla `[active]`-attribuutilla, ja teemassa on sille selvät tyylit: vasen primary-värinen aksenttipalkki, primary-color-10pct taustatäyttö, lihavoitu fontti ja inset-varjo. Hover-tilalle on kevyt taustahighlight ja 160ms transition.

#### D5. Footer: tekijä, copyright, lisätieto; pinned + responsiivinen
- Tila: [x]
- Todiste: `MainLayout.java` (`createApplicationFooter()` + `addToDrawer(createApplicationDrawer(), createApplicationFooter())`); `themes/movie/styles.css` osio `.app-footer*` (`:hover`, `:focus-visible`, `@media (max-width: 480px)`).
- Miten toteutettu: Footer kiinnitetään AppLayoutin drawerin alaosaan, jolloin se pysyy aina viewportin alalaidassa **ilman erillistä CSS-positiota** — tämä on AppLayoutin oletuskäyttäytyminen. Sisältö on kolmella rivillä: `© <kuluva vuosi> Toni Piispa` (vuosi luetaan `Year.now()`-metodilla, ei kovakoodattu), GitHub-linkki uuteen välilehteen (`target="_blank"` + `rel="noopener noreferrer"`) ja lyhyt "Vaadin coursework"-lisäteksti. Visuaalisesti footerilla on parchment-gradient, primary-värinen yläborder ja linkin hover/focus-tilat. **Responsiivinen** kahdella tavalla: (1) drawer pakkautuu Vaadinin oletuksilla mobiilissa overlay-tilaan, ja (2) `@media (max-width: 480px)` kutistaa paddingia ja piilottaa lisätekstin pienellä näytöllä.

---

### E. Autentikointi ja tietoturva

#### E1. Spring Security + käyttäjät + roolit + hash-salasana
- Tila: [x]
- Todiste: `User.java`, `Role.java`, `UserRepository.java`, `AppUserDetailsService.java`, `SecurityConfig.java`, `UserSeeder.java`, `LoginView.java`.
- Miten toteutettu: Spring Security konfiguroidaan Vaadin 25:n `VaadinSecurityConfigurer`-pohjalta (`SecurityFilterChain`-bean). `User`-entiteetillä on `Role`-enumi (`ADMIN`, `SUPER`, `USER`) ja salasana tallennetaan `BCryptPasswordEncoder`-hashina, ei selvätekstinä. Idempotentti `UserSeeder` (`CommandLineRunner`) luo käynnistyksessä kolme testitiliä jos niitä ei vielä ole: `Admin/admin123`, `Super/super123`, `User/user123`. Kirjautuminen tapahtuu `LoginView`ssa.

#### E2. Roolipohjaiset pääsyoikeudet eri sivuille
- Tila: [x]
- Todiste:
  - `HomeView.java` — `@AnonymousAllowed`
  - `MovieListView`, `MovieStatsListView`, `ShowtimeListView`, `GenreListView`, `MovieSearchView` — `@PermitAll`
  - `MemberLoungeView.java` — `@RolesAllowed({"SUPER","USER"})`
  - `AdminConsoleView.java` — `@RolesAllowed("ADMIN")`
  - Roolit `Role.java`; `AppUserDetailsService` mappaa `ROLE_*`-authoriteetit; `UserSeeder` tarjoaa Admin/Super/User-tilit.
- Miten toteutettu: Pääsymatriisi on neljätasoinen ja toteutetaan kokonaan reittikohtaisilla annotaatioilla. Vaadinin `VaadinSecurityConfigurer` lukee `@AnonymousAllowed`/`@PermitAll`/`@RolesAllowed`-merkinnät ja torjuu pääsyn ennen näkymän rendaamista.

  | Reitti | Annotaatio | Anonyymi | USER | SUPER | ADMIN |
  |---|---|:-:|:-:|:-:|:-:|
  | `/` | `@AnonymousAllowed` | sallittu | sallittu | sallittu | sallittu |
  | `/movies`, `/showtimes`, `/genres`, `/movie-stats`, `/movies/search` | `@PermitAll` | login | sallittu | sallittu | sallittu |
  | `/lounge` | `@RolesAllowed({"SUPER","USER"})` | login | sallittu | sallittu | denied |
  | `/admin/console` | `@RolesAllowed("ADMIN")` | login | denied | denied | sallittu |

#### E3. Rekisteröitymissivu
- Tila: [x]
- Todiste: `UserRegistrationService.java`; `RegistrationView.java` (reitti `/register`, `@AnonymousAllowed`); `LoginView.java` (linkki "Create an account").
- Miten toteutettu: `UserRegistrationService` (`@Transactional`) tarkistaa käyttäjätunnuksen ainutlaatuisuuden, hashaa salasanan olemassa olevalla `BCryptPasswordEncoder`-beanillä ja tallentaa uuden `User`-rivin oletusroolilla `USER`. `RegistrationView` on julkinen lomake (käyttäjätunnus + salasana + vahvistus), jossa on kenttätason validointi (pituudet, vahvistuksen yhtenevyys) ja palvelinpään duplikaattitarkistus virheviesteineen. Onnistuneen rekisteröitymisen jälkeen näytetään ilmoitus ja ohjataan kirjautumissivulle.

#### E4. Kustomoitu virheviesti puuttuvista oikeuksista
- Tila: [x]
- Todiste: `AccessDeniedView.java` (`implements HasErrorParameter<AccessDeniedException>`, `setErrorParameter` palauttaa `HttpStatusCode.FORBIDDEN.getCode()`, luokka `@AnonymousAllowed`).
- Miten toteutettu: Vaadin laukaisee `com.vaadin.flow.router.AccessDeniedException`-poikkeuksen aina kun käyttäjän rooli ei riitä reitille (esim. USER → `/admin/console`). `AccessDeniedView` toteuttaa `HasErrorParameter<AccessDeniedException>`-rajapinnan, jolloin Vaadin reitittää virheen automaattisesti tähän luokkaan ilman erillistä `@Route`a tai handler-rekisteröintiä. `setErrorParameter` palauttaa HTTP-statuksen 403, ja konstruktorissa rakennetaan käyttäjäystävällinen sisältö (otsikko "Access denied", selittävä kappale, "Back to home"-painike). `@AnonymousAllowed` varmistaa, että näkymä renderöityy myös kirjautumattomalle.

#### E5. Oman kuvan lisäys käyttäjälle
- Tila: [x]
- Todiste: `User.java` (`byte[] profilePicture` `@Basic(fetch=LAZY)` + `columnDefinition = "bytea"` + `String profilePictureMimeType`); `UserProfileService.java` (`findByUsername`, `updateProfilePicture`, `removeProfilePicture`, `MAX_PROFILE_PICTURE_BYTES = 5 MB`); `ProfilePictureDialog.java` (Vaadin `Upload` + esikatselu + Save/Remove/Cancel); `MainLayout.java` (`buildUserAvatar`, `applyAvatarImage`, `openProfilePictureDialog`).
- Miten toteutettu: Profiilikuvat tallennetaan suoraan tietokantaan `bytea`-sarakkeena (`byte[]`-kenttä lazy-fetchillä, jotta tavalliset User-haut eivät turhaan lataa kuvabytejä). Näin vältetään erillinen tiedostojärjestelmä-volumi ja kuva pysyy samassa transaktiossa kuin käyttäjätieto. Yläraja on 5 MB (`MAX_PROFILE_PICTURE_BYTES`). **UI-virta**: headerin käyttäjäkuvake on Vaadinin `Avatar`, joka näyttää kuvan `StreamResource`in kautta tai tunnuksen alkukirjaimet jos kuvaa ei ole. Klikkaus avaa `ProfilePictureDialog`in (96px-esikatselu, `Upload` + `MemoryBuffer`, mime-tyypit `image/jpeg` + `image/png`, Save / Remove / Cancel). Save ja Remove käyttävät callbackia, joka päivittää headerin avatarin uudella resurssilla ilman sivun uudelleenlataamista. Postgres-volumessa kuva säilyy myös `docker compose down`/`up`-kierroksen yli.

#### E6. OAuth-kirjautuminen (Gmail tai GitHub)
- Tila: [ ]
- Todiste (tiedosto/luokka/nakyma):
- Miten toteutettu:

---

### F. Muut toiminnallisuudet

#### F1. Julkaisu GitHubiin
- Tila: [x]
- Todiste: GitHub-repo `https://github.com/dqviii/vaadin-rojekti` (origin remote `main`-haaralla).
- Miten toteutettu: Projekti pushattu GitHubiin, `main` seuraa `origin/main`ia.

#### F2. Vaadin Server Push
- Tila: [x]
- Todiste: `Application.java` (`@Push`); `Broadcaster.java` (Spring `@Component`, `register`/`broadcast` + daemon-thread); `MovieService.java` (`broadcaster.broadcast(TOPIC)` `save`/`delete`-kutsujen jälkeen); `MovieListView.java` (`onAttach`/`onDetach` + `ui.access(() -> grid.getDataProvider().refreshAll())`).
- Miten toteutettu: `@Push` avaa WebSocket-yhteyden. Pub/sub on omassa `Broadcaster`-komponentissa, jossa kuuntelijat ajetaan dedikoidulla daemon-threadilla — näin julkaiseva tietokantatransaktio ehtii commitoitua ennen kuin kuuntelija kysyy uutta dataa. `MovieService` broadcastaa `save`/`delete`-kutsujen jälkeen, ja `MovieListView` rekisteröi/poistaa kuuntelijan `onAttach`/`onDetach`issa kutsuen `grid.getDataProvider().refreshAll()` Vaadinin UI-lukon kanssa. **Verifioitu kahdella selainvälilehdellä**: kun yhdellä luodaan/poistetaan elokuva, toinen `/movies`-välilehti päivittyy automaattisesti.

#### F3. Lokalisointi (esim. suomi/englanti)
- Tila: [x]
- Todiste: `messages.properties` (englanti, oletus); `messages_fi.properties` (suomi); `MovieI18NProvider.java` (Spring `@Component`, toteuttaa `I18NProvider`-rajapinnan); `MainLayout.java` (`createLanguageSwitch()` headerissa, `Select<Locale>`); `HomeView.java` (`getTranslation("home.*")`-kutsut).
- Miten toteutettu: Lokalisointi koostuu kolmesta osasta: (1) **resurssipaketit** tarjoavat 8 avainta home-näkymän teksteille (`home.title`, `home.intro`, `home.stat.movies`/`showtimes`/`genres`/`stats`, `home.recent.title`, `home.recent.empty`); (2) **`MovieI18NProvider`** lukee paketit `ResourceBundle.getBundle("messages", locale)`-kutsulla ja palauttaa `getProvidedLocales()`-listana `[Locale.ENGLISH, Locale.of("fi")]`; (3) **header-vaihtokytkin** on `Select<Locale>`, jonka arvonmuutos kutsuu `VaadinSession.setLocale(...)` + `UI.getPage().reload()`. HomeView'n kovakoodatut tekstit on korvattu `getTranslation("home.*")`-kutsuilla ja päivämääräformaatti käyttää `getLocale()`a. Ainoastaan HomeView on lokalisoitu.

#### F4. Docker-image + toimiva `Dockerfile`
- Tila: [x]
- Todiste: `Dockerfile` (multi-stage); ajossa `http://localhost:8080` → `LoginView` renderöityy, kirjautuminen seedatulla käyttäjällä onnistuu, `/movies`/`/movies/search`/`/register` toimivat kontissa.
- Miten toteutettu: Dockerfile on kaksivaiheinen: `eclipse-temurin:21-jdk` -build-vaihe ajaa `./mvnw clean package -DskipTests` (Vaadin Pro/Offline-avaimet voidaan antaa Docker-secretteinä `proKey`/`offlineKey`), ja `eclipse-temurin:21-jre-alpine` -runtime-vaihe ajaa valmiin jarin `--spring.profiles.active=prod` -lipulla. Image rakennetaan `docker build -t vaadin-movie-app .` ja ajetaan `docker run --rm -p 8080:8080 --name movie-app vaadin-movie-app`. Pelkän Dockerfilen kanssa tietokantana toimii in-memory H2 + `UserSeeder` joka käynnistyksessä — pysyvä Postgres tulee seuraavassa osiossa.

#### F5. `docker-compose` (tietokanta + sovellus)
- Tila: [x]
- Todiste: `docker-compose.yml` (palvelut `db` ja `app` + nimetty volume `db-data`); `pom.xml` (uusi `org.postgresql:postgresql` runtime-dep); `application-prod.properties` (prod-profiilin asetukset).
- Miten toteutettu: `db`-palvelu on `postgres:16-alpine` `pg_isready`-healthcheckillä ja nimetyllä `db-data`-volumella. `app`-palvelu rakennetaan Dockerfilesta ja käyttää `depends_on: db: condition: service_healthy`-ehtoa, joten Spring Boot käynnistyy vasta kun Postgres vastaa; datasource-arvot annetaan ympäristömuuttujina (`SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/movies`). `pom.xml`:ään lisättiin `org.postgresql:postgresql` runtime-scopella — luokkapolulla on sekä H2- että Postgres-driver, ajonaikainen valinta tulee URL:sta. Prod-spesifit asetukset (`vaadin.launch-browser=false`, datasource ympäristömuuttujista, `spring.jpa.open-in-view=false`) on eristetty `application-prod.properties`iin. Lokaalikehitys ei muutu — `./mvnw` ajaa edelleen H2:lla. Käyttöönotto: `docker compose up --build`. Volumen pyyhintä: `docker compose down -v`.

#### F6. Sahkoposti adminille uuden kayttajan luonnista
- Tila: [ ]
- Todiste (tiedosto/luokka/nakyma):
- Miten toteutettu:

#### F7. Salasanan vaihto sahkopostin avulla
- Tila: [ ]
- Todiste (tiedosto/luokka/nakyma):
- Miten toteutettu:

#### F8. Tiedoston lataus ja tallennus
- Tila: [ ]
- Todiste (tiedosto/luokka/nakyma):
- Miten toteutettu:

#### F9. CSV- tai Excel-tuonti/vienti
- Tila: [ ]
- Todiste (tiedosto/luokka/nakyma):
- Miten toteutettu:

#### F10. Spring Data Auditing
- Tila: [x]
- Todiste: `AuditableEntity.java` (`@MappedSuperclass` + `@EntityListeners(AuditingEntityListener.class)` + neljä audit-kenttää); `AuditingConfig.java` (`@EnableJpaAuditing` + `AuditorAware<String>`-bean); `Movie.java`, `MovieStats.java`, `Showtime.java`, `Genre.java` (kaikki `extends AuditableEntity`).
- Miten toteutettu: `AuditingConfig` aktivoi auditingin `@EnableJpaAuditing`illa. `AuditorAware<String>`-bean lukee `SecurityContextHolder`ista kirjautuneen käyttäjän tunnuksen; jos autentikointia ei ole (esim. seederit), palautetaan `"system"`. `AuditableEntity`-superclass tuo neljä kenttää (`createdBy`/`createdAt`/`updatedBy`/`updatedAt`), jotka kaikki domain-entiteetit perivät — `User` jätetään tarkoituksella ulkopuolelle. Arvot asetetaan automaattisesti INSERT/UPDATE-operaatioissa ilman service-kerrokseen koskemista. Varmistus SQL:lla: `select title, created_by, updated_by from movie limit 5;`.

#### F11. Historiatieto jokaisesta entiteettimuutoksesta
- Tila: [x]
- Todiste: `pom.xml` (`org.hibernate.orm:hibernate-envers`); `Movie.java`, `MovieStats.java`, `Showtime.java`, `Genre.java`, `AuditableEntity.java` (kaikki `@Audited`).
- Miten toteutettu: Envers kytketään päälle yhdellä riippuvuudella (versio Spring Boot BOMista). Kaikki neljä domain-entiteettiä on `@Audited`, ja koska Envers vaatii `@Audited`in myös `@MappedSuperclass`ille jotta perityt kentät tracketaan, se on lisätty `AuditableEntity`yn. Envers luo automaattisesti `_aud`-companion-taulut (`movie_aud`, `movie_stats_aud`, `showtime_aud`, `genre_aud`, `movie_genre_aud`) ja yhteisen `revinfo`-taulun. Jokainen INSERT/UPDATE/DELETE kirjoittaa rivin `_aud`-tauluun revisiotyypillä 0/1/2. F10:n audit-sarakkeet kulkevat snapshoteihin perinnän mukana, joten Envers säilöö myös tekijän.

#### F12. Historiatiedon näyttö käyttöliittymässä
- Tila: [x]
- Todiste: `MovieRevision.java` (DTO-record); `MovieHistoryService.java` (Envers `AuditReader`-haku); `MovieHistoryDialog.java` (`Dialog` + `Grid<MovieRevision>`); `MovieListView.java` (kellokuvake-painike rivillä, `openHistory`-metodi).
- Miten toteutettu: `MovieHistoryService` (`@Transactional(readOnly=true)`) hakee Enversin `AuditReader`illa elokuvan revisiot uusin ensin -järjestyksessä ja mappaa tuloksen `MovieRevision`-recordiksi. "By"-arvo otetaan ADD-revisioissa `createdBy`-kentästä ja MOD/DEL-revisioissa `updatedBy`-kentästä (paluulinjana `createdBy`). `MovieHistoryDialog` näyttää tulokset Gridissä kuudella sarakkeella (Rev / Change / When / Title / Director / By); aikaleima muotoillaan locale-tietoisesti, ja DELETE-revisioiden null-arvot näytetään emdash-merkkinä. `MovieListView`n riveille on lisätty kellokuvake-painike, joka avaa dialogin.

#### F13. Ulkoinen JavaScript-komponentti (esim. Quill.js)
- Tila: [x]
- Todiste: `src/main/frontend/quill-editor.js` (web component); `QuillEditor.java` (`@NpmPackage("quill", "2.0.2")` + `@JsModule("./quill-editor.js")`); `Genre.java` (description `TEXT`, `@Size(max=5000)`); `GenreListView.java` (TextField korvattu `QuillEditor`illa).
- Miten toteutettu: Käytössä on **Quill 2.x** (ei Vaadinin oma `RichTextEditor`). `quill-editor.js` kääräisee Quillin `<quill-editor>`-custom-elementiksi ja lähettää muutoksista `value-changed`-eventin. Java-puolen `QuillEditor` on `@NpmPackage`/`@JsModule`-annotoitu wrapper, joka periytyy `AbstractSinglePropertyField<QuillEditor, String>`-luokasta — tämä hoitaa property-synkronoinnin ja tekee komponentista suoraan `BeanValidationBinder`-yhteensopivan. `GenreListView`n description-kentässä TextField vaihdettiin `QuillEditor`iksi, ja `Genre.description` levennettiin `varchar(240)` → `TEXT` + `@Size(max=5000)` jotta HTML-output mahtuu. Gridin description-sarake näyttää sisällön plain-textinä `stripHtml`-utility-metodin kautta.

---

## Kehitysaikainen seedaus

Sovellus seedaa käynnistyksessä testidatan, jotta UI on heti käytettävässä kunnossa. Seederit ovat idempotentteja, joten ne ajavat vain puuttuvat rivit — käyttäjän itse lisäämiä tai muokkaamia rivejä ei korvata. Lokaalisti H2-tietokanta tyhjenee jokaisella uudelleenkäynnistyksellä, joten seederit ajavat joka kerta uudestaan; Docker-pinossa Postgres säilyy `db-data`-volumessa, jolloin seederit menevät läpi tyhjäkäynnillä ensimmäisen pyörityksen jälkeen.

- `src/main/java/com/tonip/security/UserSeeder.java` luo kolme testitiliä: `Admin/admin123` (ADMIN), `Super/super123` (SUPER), `User/user123` (USER). Käytössä E2-pääsymatriisin testaamiseen kaikilla rooleilla.
- `src/main/java/com/tonip/movie/MovieCatalogSeeder.java` luo:
  - 10 genreä 
  - 10 tunnettua elokuvaa genreliitoksin.
  - Jokaiselle elokuvalle `MovieStats`-rivin (budjetti, lipputulot, kesto, IMDB-arvosana, arvostelujen määrä).
  - Kolme `Showtime`-riviä toukokuun 2026 päivämäärille.

  Idempotenssi tarkistetaan `existsByGenreNameIgnoreCase`-, `existsByTitleIgnoreCase`- ja `existsByMovieId`-metodeilla.

## Project Structure

The project follows a *feature-based package structure*. Each top-level package under `com.tonip` is a self-contained
feature (domain entities, services, UI views, repositories) rather than being split by architectural layer.

```
.
├── docker-compose.yml                          F5 — Postgres + app stack
├── Dockerfile                                  F4 — multi-stage Vaadin build
├── pom.xml                                     dependencies (Vaadin, Spring Boot, JPA, Envers, Postgres, H2)
└── src
    ├── main/java/com/tonip
    │   ├── Application.java                    @SpringBootApplication, @Theme("movie"), @Push (F2)
    │   ├── access                              role-based demo views (E2, E4)
    │   │   ├── AccessDeniedView.java           HasErrorParameter<AccessDeniedException> (E4)
    │   │   ├── AdminConsoleView.java           @RolesAllowed("ADMIN")
    │   │   └── MemberLoungeView.java           @RolesAllowed({"SUPER","USER"}) + LumoUtility (C4)
    │   ├── base                                cross-cutting infra
    │   │   ├── Broadcaster.java                pub/sub for server push (F2)
    │   │   ├── MovieI18NProvider.java          I18NProvider EN/FI (F3)
    │   │   ├── audit
    │   │   │   ├── AuditableEntity.java        @MappedSuperclass with @CreatedBy/Date (F10)
    │   │   │   └── AuditingConfig.java         @EnableJpaAuditing + AuditorAware (F10)
    │   │   └── ui
    │   │       ├── MainLayout.java             AppLayout, header, drawer, footer, language switch
    │   │       ├── QuillEditor.java            @JsModule + @NpmPackage Quill wrapper (F13)
    │   │       └── ViewTitle.java
    │   ├── home
    │   │   └── HomeView.java                   public dashboard (D2), translated via getTranslation (F3)
    │   ├── movie                               primary domain
    │   │   ├── MovieCatalogSeeder.java         idempotent dev seed (10 genres + 10 movies + stats + 3 showtimes)
    │   │   ├── MovieSearchCriteria.java        record carrying filter values (B-tasks)
    │   │   ├── MovieSearchService.java         Criteria API: between, JOIN, OR-AND (B2-B5)
    │   │   ├── MovieService.java               CRUD + broadcast on save/delete (F2)
    │   │   ├── GenreService.java
    │   │   ├── MovieStatsService.java
    │   │   ├── ShowtimeService.java
    │   │   ├── domain                          @Entity classes + repositories
    │   │   │   ├── Movie.java                  @Audited, extends AuditableEntity (A1, F10, F11)
    │   │   │   ├── MovieStats.java             1:1 to Movie (A2)
    │   │   │   ├── Showtime.java               N:1 to Movie (A3)
    │   │   │   ├── Genre.java                  M:N to Movie via movie_genre (A4)
    │   │   │   ├── AgeRating.java              ScreenType.java, TargetAudience.java (enums)
    │   │   │   └── *Repository.java            JpaRepository + JpaSpecificationExecutor
    │   │   ├── history                         F12 — Envers-backed history
    │   │   │   ├── MovieHistoryService.java    AuditReader query
    │   │   │   └── MovieRevision.java          DTO record
    │   │   └── ui
    │   │       ├── MovieListView.java          CRUD + history button + push refresh
    │   │       ├── MovieHistoryDialog.java     Grid<MovieRevision>
    │   │       ├── MovieSearchView.java        SplitLayout filter sidebar + results grid
    │   │       ├── MovieStatsListView.java
    │   │       ├── ShowtimeListView.java
    │   │       └── GenreListView.java          uses QuillEditor for description (F13)
    │   └── security
    │       ├── AppUserDetailsService.java      maps User → ROLE_*
    │       ├── SecurityConfig.java             VaadinSecurityConfigurer
    │       ├── UserSeeder.java                 seeds Admin/Super/User accounts (E1)
    │       ├── UserRegistrationService.java    public sign-up (E3)
    │       ├── UserProfileService.java         profile picture CRUD (E5)
    │       ├── domain
    │       │   ├── User.java                   includes profile_picture bytea column (E5)
    │       │   ├── Role.java
    │       │   └── UserRepository.java
    │       └── ui
    │           ├── LoginView.java
    │           ├── RegistrationView.java
    │           └── ProfilePictureDialog.java   Upload + 5 MB cap (E5)
    ├── main/resources
    │   ├── application.properties              dev defaults (H2 in-memory)
    │   ├── application-prod.properties         prod profile (Postgres via env vars)
    │   ├── messages.properties                 EN i18n bundle (F3)
    │   └── messages_fi.properties              FI i18n bundle (F3)
    └── main/frontend
        ├── quill-editor.js                     web component shim around Quill (F13)
        ├── styles
        │   └── access-denied.css               view-scoped CSS via @CssImport (C3, C5)
        └── themes/movie
            ├── styles.css                      retro-cinema palette + utility rules (C1, C2)
            └── theme.json
```

The main entry point is `Application.java` — `@SpringBootApplication`, `@Theme("movie")`, `@Push`, and
`AppShellConfigurator`. Cross-cutting infrastructure lives under `com.tonip.base` (broadcasting, i18n, auditing,
shared UI). Domain logic is grouped per feature: `movie` (the primary CRUD/search domain plus its history view),
`security` (authentication, roles, registration, profile picture), `home` (public dashboard), and `access`
(role-gated demo views for E2/E4).


