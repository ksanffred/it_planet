-- Seed employers
INSERT INTO employers (id, name, logo_url, website, contacts)
VALUES (1, 'Acme Corp', 'https://acme.com/logo.png', 'https://acme.com', 'hr@acme.com')
ON CONFLICT (id) DO NOTHING;

-- Seed tags: technologies
INSERT INTO tags (id, name, category) VALUES
    (1,  'Python',      'TECHNOLOGY'),
    (2,  'Java',        'TECHNOLOGY'),
    (3,  'SQL',         'TECHNOLOGY'),
    (4,  'JavaScript',  'TECHNOLOGY'),
    (5,  'TypeScript',  'TECHNOLOGY'),
    (6,  'Go',          'TECHNOLOGY'),
    (7,  'Kotlin',      'TECHNOLOGY'),
    (8,  'C++',         'TECHNOLOGY'),
    (9,  'React',       'TECHNOLOGY'),
    (10, 'Spring Boot', 'TECHNOLOGY'),
-- Seed tags: levels
    (11, 'Junior',      'LEVEL'),
    (12, 'Middle',      'LEVEL'),
    (13, 'Senior',      'LEVEL'),
    (14, 'Intern',      'LEVEL'),
-- Seed tags: employment types
    (15, 'Full-time',   'EMPLOYMENT_TYPE'),
    (16, 'Part-time',   'EMPLOYMENT_TYPE'),
    (17, 'Project',     'EMPLOYMENT_TYPE'),
    (18, 'Freelance',   'EMPLOYMENT_TYPE')
ON CONFLICT (id) DO NOTHING;

-- Reset sequences to avoid conflicts with Hibernate-generated IDs
SELECT setval('employers_id_seq', (SELECT MAX(id) FROM employers));
SELECT setval('tags_id_seq',      (SELECT MAX(id) FROM tags));
