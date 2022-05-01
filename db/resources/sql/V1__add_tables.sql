CREATE TABLE users(
  id SERIAL PRIMARY KEY,
  name text not null
);

CREATE TABLE phone_numbers (
  id SERIAL PRIMARY KEY,
  user_id INT REFERENCES users(id) NOT NULL,
  phone_number text NOT null
)
