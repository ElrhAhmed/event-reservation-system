INSERT INTO users (email, password, nom, prenom, role, actif, date_inscription)
VALUES
    ('admin@event.ma',
     '$2a$10$16sBH/Ft9vgVKNXJoYMiIu2WHvvuFHOnz3AwvB97ZJqTcxRRwtPXW',
     'Admin','System','ADMIN',true,NOW()),

    ('organizer1@event.ma',
     '$2a$10$16sBH/Ft9vgVKNXJoYMiIu2WHvvuFHOnz3AwvB97ZJqTcxRRwtPXW',
     'Organisateur','Alpha','ORGANIZER',true,NOW()),

    ('organizer2@event.ma',
     '$2a$10$16sBH/Ft9vgVKNXJoYMiIu2WHvvuFHOnz3AwvB97ZJqTcxRRwtPXW',
     'Organisateur','Beta','ORGANIZER',true,NOW()),

    ('client1@event.ma',
     '$2a$10$16sBH/Ft9vgVKNXJoYMiIu2WHvvuFHOnz3AwvB97ZJqTcxRRwtPXW',
     'Dupont','Jean','CLIENT',true,NOW()),

    ('client2@event.ma',
     '$2a$10$16sBH/Ft9vgVKNXJoYMiIu2WHvvuFHOnz3AwvB97ZJqTcxRRwtPXW',
     'Alami','Sara','CLIENT',true,NOW());



INSERT INTO events
(titre, description, categorie, date_debut, date_fin, lieu, ville,
 capacite_max, prix_unitaire, statut, image_url,
 organisateur_id, date_creation, date_modification)
VALUES
    ('Mawazine 2026','Festival musique','CONCERT',
     DATEADD('MONTH',3,NOW()),DATEADD('MONTH',3,NOW()),
     'OLM Souissi','Rabat',5000,200,'PUBLIE',
     '/images/events/concert.jpg',2,NOW(),NOW()),

    ('Jazzablanca','Jazz festival','CONCERT',
     DATEADD('MONTH',1,NOW()),DATEADD('MONTH',1,NOW()),
     'Anfa Place','Casablanca',2000,150,'PUBLIE',
     '/images/events/concert.jpg',2,NOW(),NOW()),

    ('Rock Tanger','Rock live','CONCERT',
     DATEADD('MONTH',2,NOW()),DATEADD('MONTH',2,NOW()),
     'Malabata','Tanger',3000,180,'ANNULE',
     '/images/events/concert.jpg',3,NOW(),NOW()),

    ('Antigone','Théâtre classique','THEATRE',
     DATEADD('MONTH',1,NOW()),DATEADD('MONTH',1,NOW()),
     'Théâtre Royal','Marrakech',300,120,'PUBLIE',
     '/images/events/theatre.jpg',3,NOW(),NOW()),

    ('Hamlet','Shakespeare','THEATRE',
     DATEADD('MONTH',-1,NOW()),DATEADD('MONTH',-1,NOW()),
     'Centre Culturel','Fès',250,100,'TERMINE',
     '/images/events/theatre.jpg',2,NOW(),NOW()),

    ('Devoxx Morocco','Conférence dev','CONFERENCE',
     DATEADD('DAY',10,NOW()),DATEADD('DAY',12,NOW()),
     'Hilton','Taghazout',400,500,'PUBLIE',
     '/images/events/conference.jpg',2,NOW(),NOW()),

    ('AI Summit','IA & Innovation','CONFERENCE',
     DATEADD('MONTH',1,NOW()),DATEADD('MONTH',1,NOW()),
     'Technopark','Casablanca',350,450,'PUBLIE',
     '/images/events/conference.jpg',3,NOW(),NOW()),

    ('Marathon Casa','Sport','SPORT',
     DATEADD('MONTH',-1,NOW()),DATEADD('MONTH',-1,NOW()),
     'Centre Ville','Casablanca',2000,50,'TERMINE',
     '/images/events/sport.jpg',3,NOW(),NOW()),

    ('Trail Atlas','Montagne','SPORT',
     DATEADD('MONTH',2,NOW()),DATEADD('MONTH',2,NOW()),
     'Atlas','Marrakech',800,120,'PUBLIE',
     '/images/events/sport.jpg',2,NOW(),NOW()),

    ('Salon Livre','Culture','AUTRE',
     DATEADD('MONTH',1,NOW()),DATEADD('MONTH',1,NOW()),
     'Expo Center','Rabat',1000,60,'PUBLIE',
     '/images/events/autre.jpg',2,NOW(),NOW());



INSERT INTO reservations
(code_reservation, date_reservation, nombre_places,
 montant_total, statut, user_id, event_id)
VALUES
    ('EVT-001',DATEADD('DAY',-5,NOW()),2,400,'CONFIRMEE',4,1),
    ('EVT-002',DATEADD('DAY',-3,NOW()),1,150,'CONFIRMEE',4,2),
    ('EVT-003',DATEADD('DAY',-2,NOW()),3,360,'EN_ATTENTE',4,4),
    ('EVT-004',DATEADD('DAY',-1,NOW()),4,800,'CONFIRMEE',5,6),
    ('EVT-005',DATEADD('DAY',-6,NOW()),2,240,'ANNULEE',5,4),
    ('EVT-006',DATEADD('DAY',-4,NOW()),5,1000,'CONFIRMEE',5,1),
    ('EVT-007',DATEADD('DAY',-2,NOW()),1,60,'CONFIRMEE',4,10),
    ('EVT-008',NOW(),2,120,'CONFIRMEE',5,10),
    ('EVT-009', DATEADD('DAY',-7,NOW()), 3, 600, 'CONFIRMEE', 4, 1),   -- 3×200
    ('EVT-010', DATEADD('DAY',-2,NOW()), 2, 300, 'CONFIRMEE', 5, 2),   -- 2×150
    ('EVT-011', DATEADD('DAY',-1,NOW()), 1, 180, 'EN_ATTENTE', 4, 3),  -- 1×180
    ('EVT-012', DATEADD('DAY',-8,NOW()), 4, 480, 'CONFIRMEE', 5, 4),   -- 4×120
    ('EVT-013', DATEADD('DAY',-6,NOW()), 2, 200, 'ANNULEE', 4, 5),     -- 2×100
    ('EVT-014', DATEADD('DAY',-3,NOW()), 1, 500, 'CONFIRMEE', 5, 6),   -- 1×500
    ('EVT-015', DATEADD('DAY',-2,NOW()), 2, 900, 'CONFIRMEE', 4, 7),   -- 2×450
    ('EVT-016', DATEADD('DAY',-5,NOW()), 5, 250, 'CONFIRMEE', 5, 8),   -- 5×50
    ('EVT-017', DATEADD('DAY',-4,NOW()), 3, 360, 'EN_ATTENTE', 4, 9);
