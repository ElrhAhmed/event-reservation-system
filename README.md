# FESTIVENT - Système de Réservation d'Événements

## 📖 Description du Projet

FESTIVENT est une plateforme web complète de gestion et de réservation d'événements culturels développée avec Spring Boot 3.5.8 et Vaadin 24.9.6. Le système permet aux organisateurs de publier leurs événements et aux clients de réserver des places en ligne avec une gestion complète des rôles et de la sécurité.


### Fonctionnalités Principales
- Gestion des utilisateurs avec 3 rôles : Administrateur, Organisateur, Client
- Création et publication d'événements (Concerts, Théâtre, Conférences, Sport)
- Système de réservation avec génération de codes uniques
- Recherche et filtrage avancés
- Automatisation avec tâches planifiées

---

## 🛠 Technologies Utilisées

| Technologie | Version | Description |
|-------------|---------|-------------|
| **Java** | 17 | Langage de programmation |
| **Spring Boot** | 3.5.8 | Framework backend |
| **Spring Data JPA** | 3.5.8 | Persistance des données |
| **Spring Security** | 6 | Authentification et autorisation |
| **Spring Validation** | 3.5.8 | Validation des données |
| **Vaadin** | 24.9.6 | Framework UI Java full-stack |
| **H2 Database** | 2 | Base de données embarquée |
| **Lombok** | Latest | Réduction du code boilerplate |
| **Maven** | 3.8+ | Gestion des dépendances |

---

## ⚙️ Prérequis

Avant de commencer, assurez-vous d'avoir installé les éléments suivants :

- **JDK 17** ou supérieur - [Télécharger Oracle JDK](https://www.oracle.com/java/technologies/downloads/)
- **Maven 3.8+** - [Télécharger Maven](https://maven.apache.org/download.cgi)
- Un IDE Java (recommandé : IntelliJ IDEA, Eclipse ou VS Code)



## 🚀 Installation

### 1. Cloner le Repository

```bash
git clone https://github.com/ElrhAhmed/festivent.git
cd festivent
```

### 2. Installer les Dépendances

```bash
mvn clean install
```

Cette commande va :
- Télécharger toutes les dépendances nécessaires
- Compiler le projet


---

## 🗄️ Configuration de la Base de Données

### Base de Données H2 (Embarquée)

L'application utilise **H2 Database** en mode mémoire. Aucune installation supplémentaire n'est requise.

**Configuration automatique** via `application.properties` 
```

```

### Accès à la Console H2

Une fois l'application lancée, vous pouvez accéder à la console H2 :

- **URL** : `http://localhost:8080/h2-console`
- **JDBC URL** : `jdbc:h2:mem:eventdb`
- **Username** : `sa`
- **Password** : *(laisser vide)*
```



 ```
### Données de Test

Le fichier `data.sql` charge automatiquement :
- **5 utilisateurs** (1 admin, 2 organisateurs, 2 clients)
- **10 événements** exemple
- **17 réservations** de démonstration

```

```

## ▶️ Instructions de Lancement

### Avec IntelliJ IDEA 

1. **Ouvrir le projet**
   - File → Open → Sélectionnez le dossier `festivent`
   - IntelliJ détectera automatiquement le projet Maven

2. **Lancer l'application**
   - Naviguez vers `src/main/java/ma/projet/events/Festivent.java`
   - Cliquez sur le bouton ▶️ vert à côté de la classe
   - Sélectionnez "Run 'Festivent'"

   ```
    ```
   

   ### Avec Maven 
```bash
mvn spring-boot:run
```
 ```


 ```
### Accès à l'Application

Une fois lancée, l'application est accessible sur :

```
http://localhost:8080
```

---

## 👤 Comptes de Test

Utilisez ces identifiants pour tester l'application :

### Administrateur
- **Email** : `admin@event.ma`
- **Mot de passe** : `password123`
- **Accès** : Gestion complète du système

### Organisateur
- **Email** : `organizer1@event.ma` ou `organizer2@event.ma`
- **Mot de passe** : `password123`
- **Accès** : Création et gestion d'événements

### Client
- **Email** : `client1@event.ma` ou `client2@event.ma`
- **Mot de passe** : `password123`
- **Accès** : Réservation d'événements

---

