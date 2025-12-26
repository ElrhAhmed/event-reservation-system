-- ==================== FESTIVENT - DONNÉES DE TEST ====================
-- Version simplifiée et testée pour H2
-- =====================================================================

-- ==================== 1. UTILISATEURS (5) ====================

INSERT INTO users (nom, prenom, email, password, role, actif, date_inscription, telephone)
VALUES
    ('Admin', 'Systeme', 'admin@festivent.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', true, CURRENT_TIMESTAMP, '0600000001'),
    ('El Idrissi', 'Karim', 'karim.organizer@festivent.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ORGANIZER', true, CURRENT_TIMESTAMP, '0601234567'),
    ('Benjelloun', 'Sofia', 'sofia.organizer@festivent.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ORGANIZER', true, CURRENT_TIMESTAMP, '0607654321'),
    ('Benali', 'Ahmed', 'ahmed.client@festivent.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'CLIENT', true, CURRENT_TIMESTAMP, '0611223344'),
    ('Alaoui', 'Fatima', 'fatima.client@festivent.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'CLIENT', true, CURRENT_TIMESTAMP, '0622334455');

-- ==================== 2. ÉVÉNEMENTS (15) ====================

-- CONCERTS (3)
INSERT INTO events (titre, description, categorie, date_debut, date_fin, lieu, ville, capacite_max, prix_unitaire, statut, date_creation, date_modification, organisateur_id, image_url)
VALUES
    ('Festival Gnaoua 2026', 'Le plus grand festival de musique Gnaoua au Maroc', 'CONCERT', '2026-06-20 20:00:00', '2026-06-20 23:30:00', 'Place Moulay Hassan', 'Essaouira', 5000, 200.00, 'PUBLIE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 2, 'https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=800'),
    ('Concert Saad Lamjarred', 'Soiree exceptionnelle avec la star marocaine', 'CONCERT', '2026-03-15 21:00:00', '2026-03-15 23:00:00', 'Stade Mohammed V', 'Casablanca', 8000, 350.00, 'PUBLIE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 2, 'https://images.unsplash.com/photo-1470229722913-7c0e2dbbafd3?w=800'),
    ('Mawazine Festival 2026', 'Le plus grand festival de musiques du monde', 'CONCERT', '2026-05-10 19:00:00', '2026-05-10 23:00:00', 'Scene OLM Souissi', 'Rabat', 15000, 150.00, 'PUBLIE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 3, 'https://images.unsplash.com/photo-1459749411175-04bf5292ceea?w=800');

-- THEATRE (3)
INSERT INTO events (titre, description, categorie, date_debut, date_fin, lieu, ville, capacite_max, prix_unitaire, statut, date_creation, date_modification, organisateur_id, image_url)
VALUES
    ('Le Bourgeois Gentilhomme', 'Comedie-ballet de Moliere dans une mise en scene moderne', 'THEATRE', '2026-04-05 20:00:00', '2026-04-05 22:00:00', 'Theatre Mohammed V', 'Rabat', 600, 120.00, 'PUBLIE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 3, 'https://images.unsplash.com/photo-1503095396549-807759245b35?w=800'),
    ('One Man Show Gad Elmaleh', 'Le celebre humoriste en tournee au Maroc', 'THEATRE', '2026-02-28 20:30:00', '2026-02-28 22:30:00', 'Morocco Mall', 'Casablanca', 1200, 250.00, 'PUBLIE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 2, 'https://images.unsplash.com/photo-1585699324551-f6c309eedeca?w=800'),
    ('Spectacle Nass El Ghiwane', 'Legende de la musique marocaine engagee', 'THEATRE', '2026-07-14 21:00:00', '2026-07-14 23:00:00', 'Theatre National', 'Marrakech', 800, 180.00, 'PUBLIE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 3, 'https://images.unsplash.com/photo-1507676184212-d03ab07a01bf?w=800');

-- CONFERENCES (3)
INSERT INTO events (titre, description, categorie, date_debut, date_fin, lieu, ville, capacite_max, prix_unitaire, statut, date_creation, date_modification, organisateur_id, image_url)
VALUES
    ('TEDx Casablanca 2026', 'Ideas worth spreading - Innovation et entrepreneuriat', 'CONFERENCE', '2026-09-20 09:00:00', '2026-09-20 18:00:00', 'Twin Center', 'Casablanca', 500, 300.00, 'PUBLIE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 2, 'https://images.unsplash.com/photo-1505373877841-8d25f7d46678?w=800'),
    ('Sommet Tech Africa 2026', 'Conference sur les nouvelles technologies en Afrique', 'CONFERENCE', '2026-11-10 08:30:00', '2026-11-10 17:00:00', 'Palais des Congres', 'Marrakech', 1000, 500.00, 'PUBLIE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 3, 'https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=800'),
    ('Forum Economie Verte', 'Developpement durable et energies renouvelables au Maroc', 'CONFERENCE', '2026-10-05 09:00:00', '2026-10-05 16:00:00', 'Hotel Sofitel', 'Rabat', 300, 450.00, 'PUBLIE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 2, 'https://images.unsplash.com/photo-1591115765373-5207764f72e7?w=800');

-- SPORT (3)
INSERT INTO events (titre, description, categorie, date_debut, date_fin, lieu, ville, capacite_max, prix_unitaire, statut, date_creation, date_modification, organisateur_id, image_url)
VALUES
    ('Marathon de Casablanca 2026', 'Course internationale 42km a travers la ville blanche', 'SPORT', '2026-04-15 07:00:00', '2026-04-15 14:00:00', 'Corniche Ain Diab', 'Casablanca', 3000, 100.00, 'PUBLIE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 2, 'https://images.unsplash.com/photo-1452626038306-9aae5e071dd3?w=800'),
    ('Match Raja vs WAC', 'Derby casablancais - Championnat national', 'SPORT', '2026-03-22 20:00:00', '2026-03-22 22:00:00', 'Stade Mohammed V', 'Casablanca', 45000, 80.00, 'PUBLIE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 3, 'https://images.unsplash.com/photo-1579952363873-27f3bade9f55?w=800'),
    ('Rallye du Maroc 2026', 'Competition automobile dans le desert marocain', 'SPORT', '2026-10-01 08:00:00', '2026-10-05 18:00:00', 'Depart Agadir', 'Agadir', 500, 1200.00, 'PUBLIE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 2, 'https://images.unsplash.com/photo-1532649538693-f3fd1ec14e3f?w=800');

-- AUTRE (3)
INSERT INTO events (titre, description, categorie, date_debut, date_fin, lieu, ville, capacite_max, prix_unitaire, statut, date_creation, date_modification, organisateur_id, image_url)
VALUES
    ('Festival Timitar 2026', 'Celebration de la culture amazighe avec musique et arts', 'AUTRE', '2026-07-20 18:00:00', '2026-07-22 23:00:00', 'Place Al Mouahidine', 'Agadir', 10000, 50.00, 'PUBLIE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 3, 'https://images.unsplash.com/photo-1533174072545-7a4b6ad7a6c3?w=800'),
    ('Salon du Livre 2026', 'Rencontres litteraires et dedicaces auteurs', 'AUTRE', '2026-02-10 10:00:00', '2026-02-14 20:00:00', 'SIEL', 'Casablanca', 5000, 30.00, 'PUBLIE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 2, 'https://images.unsplash.com/photo-1481627834876-b7833e8f5570?w=800'),
    ('Moussem de Tan-Tan', 'Patrimoine culturel immateriel de humanite UNESCO', 'AUTRE', '2026-05-25 09:00:00', '2026-05-28 22:00:00', 'Place Centrale', 'Tan-Tan', 8000, 0.00, 'PUBLIE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 3, 'https://images.unsplash.com/photo-1533174072545-7a4b6ad7a6c3? w=800');

-- ==================== 3. RÉSERVATIONS (22) ====================

-- Reservations CLIENT Ahmed (ID 4)
INSERT INTO reservations (code_reservation, nombre_places, montant_total, date_reservation, statut, commentaire, user_id, event_id)
VALUES
    ('EVT-10001', 2, 400.00, CURRENT_TIMESTAMP, 'CONFIRMEE', NULL, 4, 1),
    ('EVT-10002', 1, 350.00, CURRENT_TIMESTAMP, 'CONFIRMEE', NULL, 4, 2),
    ('EVT-10003', 3, 360.00, CURRENT_TIMESTAMP, 'CONFIRMEE', NULL, 4, 4),
    ('EVT-10004', 2, 500.00, CURRENT_TIMESTAMP, 'CONFIRMEE', NULL, 4, 5),
    ('EVT-10005', 4, 1200.00, CURRENT_TIMESTAMP, 'EN_ATTENTE', NULL, 4, 7),
    ('EVT-10006', 1, 300.00, CURRENT_TIMESTAMP, 'CONFIRMEE', NULL, 4, 8),
    ('EVT-10007', 2, 200.00, CURRENT_TIMESTAMP, 'CONFIRMEE', NULL, 4, 10),
    ('EVT-10008', 5, 400.00, CURRENT_TIMESTAMP, 'ANNULEE', 'Annulee par le client', 4, 11),
    ('EVT-10009', 1, 1200.00, CURRENT_TIMESTAMP, 'CONFIRMEE', NULL, 4, 12),
    ('EVT-10010', 3, 150.00, CURRENT_TIMESTAMP, 'CONFIRMEE', NULL, 4, 13);

-- Reservations CLIENT Fatima (ID 5)
INSERT INTO reservations (code_reservation, nombre_places, montant_total, date_reservation, statut, commentaire, user_id, event_id)
VALUES
    ('EVT-20001', 2, 700.00, CURRENT_TIMESTAMP, 'CONFIRMEE', NULL, 5, 2),
    ('EVT-20002', 1, 150.00, CURRENT_TIMESTAMP, 'CONFIRMEE', NULL, 5, 3),
    ('EVT-20003', 2, 240.00, CURRENT_TIMESTAMP, 'CONFIRMEE', NULL, 5, 4),
    ('EVT-20004', 4, 720.00, CURRENT_TIMESTAMP, 'CONFIRMEE', NULL, 5, 6),
    ('EVT-20005', 1, 300.00, CURRENT_TIMESTAMP, 'CONFIRMEE', NULL, 5, 8),
    ('EVT-20006', 3, 1350.00, CURRENT_TIMESTAMP, 'EN_ATTENTE', NULL, 5, 9),
    ('EVT-20007', 2, 200.00, CURRENT_TIMESTAMP, 'CONFIRMEE', NULL, 5, 10),
    ('EVT-20008', 1, 80.00, CURRENT_TIMESTAMP, 'CONFIRMEE', NULL, 5, 11),
    ('EVT-20009', 2, 100.00, CURRENT_TIMESTAMP, 'CONFIRMEE', NULL, 5, 13),
    ('EVT-20010', 4, 120.00, CURRENT_TIMESTAMP, 'CONFIRMEE', NULL, 5, 14),
    ('EVT-20011', 1, 0.00, CURRENT_TIMESTAMP, 'CONFIRMEE', NULL, 5, 15),
    ('EVT-20012', 3, 600.00, CURRENT_TIMESTAMP, 'ANNULEE', 'Changement de programme', 5, 1);