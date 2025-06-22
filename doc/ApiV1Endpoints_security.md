| REST Endpoint                                      | Security                    |
|---------------------------------------------------|-----------------------------|
| GET /api/v1/imeis/{imei}/positions/last          | Owning user                 |
| GET /api/v1/imeis/{imei}/positions               |                             |
| POST /api/v1/positions                           |                             |
| POST /api/v1/authenticate                        | Open                        |
| GET /api/v1/imeis                                | Admin                       |
| GET /api/v1/imeis/{id}                           | Owning User                 |
| POST /api/v1/imeis                               | Owning User                 |
| GET /api/v1/journeys/{journey}/track             | Depends on journey settings |
| POST /api/v1/journeys                            | Depends on journey settings |
| GET /api/v1/journeys/{id}                        |                             |
| PUT /api/v1/journeys/{id}                        |                             |