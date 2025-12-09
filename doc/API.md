# myTrack REST API Documentation

This document provides a detailed, human-readable overview of the myTrack REST API endpoints.

## Base Path
All API endpoints are prefixed with `/api/v1`.

## Authentication
All endpoints require a valid JSON Web Token (JWT) to be passed in the `Authorization` header as a Bearer token.

---

## User Controller
*Base Path: `/api/v1/user`*

### Get User Features
- **GET** `/user/{username}/features`
- **Description**: Retrieves the feature flags for a specific user. The feature field is a 64-bit integer that encodes the user's domain and subscribed packages.
- **Authorization**: The authenticated user or a user with the `GOD` role.
- **Path Parameters**:
    - `username` (string): The username of the user.
- **Responses**:
    - `200 OK`: Returns a `UserFeaturesDTO` object.
        ```json
        {
          "domain": 1,
          "packages": 2
        }
        ```
    - `401 Unauthorized`, `403 Forbidden`, `404 Not Found`

---

## Journey Controller
*Base Path: `/api/v1/journeys`*

### Get Active Journey for User
- **GET** `/journeys/user/{username}/active`
- **Description**: Retrieves the currently active journey for a specific user. An active journey has a start date but no end date.
- **Authorization**: The authenticated user or a user with the `GOD` role.
- **Path Parameters**:
    - `username` (string): The username of the user.
- **Responses**:
    - `200 OK`: Returns a `JourneyDTO` object.
    - `204 No Content`: If no active journey is found.

### Get Journey by ID
- **GET** `/journeys/{id}`
- **Description**: Retrieves a single journey by its unique ID.
- **Authorization**: The journey owner or a user with the `GOD` role.
- **Responses**:
    - `200 OK`: Returns a `JourneyDTO`.

### Create Journey
- **POST** `/journeys`
- **Description**: Creates a new journey. The authenticated user will be set as the owner.
- **Request Body**: `CreateJourneyDTO`
- **Responses**:
    - `201 Created`: Returns the newly created `JourneyDTO`.

### Update Journey
- **PUT** `/journeys/{id}`
- **Description**: Updates an existing journey.
- **Authorization**: The journey owner or a user with the `GOD` role.
- **Request Body**: `CreateJourneyDTO`
- **Responses**:
    - `200 OK`: Returns the updated `JourneyDTO`.

### Delete Journey
- **DELETE** `/journeys/{id}`
- **Description**: Deletes a journey.
- **Authorization**: The journey owner or a user with the `GOD` role.
- **Responses**:
    - `204 No Content`.

### Get Journey Track
- **GET** `/journeys/{journey}/track`
- **Description**: Retrieves the track of a journey as a GeoJSON object.
- **Authorization**: The journey owner, an API client, or a user with the `GOD` role.
- **Query Parameters**:
    - `from` (string, optional): Start date/time for the track.
    - `to` (string, optional): End date/time for the track.
    - `conceal` (boolean, optional): Whether to conceal the last known position. Defaults to `true` for active journeys.
- **Responses**:
    - `200 OK`: Returns a GeoJSON `FeatureCollection`.

---

## Authentication Controller
*Base Path: `/api/v1`*

### Authenticate User
- **POST** `/authenticate`
- **Description**: Authenticates a user with a username and password and returns a JWT.
- **Request Body**:
    ```json
    {
      "username": "string",
      "password": "string"
    }
    ```
- **Responses**:
    - `200 OK`: Returns an authentication token.
        ```json
        {
          "token": "string"
        }
        ```

### Refresh Token
- **POST** `/refresh-token`
- **Description**: Obtains a new JWT using a valid, non-expired refresh token.
- **Request Body**:
    ```json
    {
      "refreshToken": "string"
    }
    ```
- **Responses**:
    - `200 OK`: Returns a new authentication token.

---

## Imei Controller
*Base Path: `/api/v1/imeis`*

### Get All IMEIs
- **GET** `/imeis`
- **Description**: Retrieves a list of all IMEI devices.
- **Authorization**: `GOD` role required.
- **Responses**:
    - `200 OK`: Returns a list of `ImeiDTO` objects.

---

## Position Controller
*Base Path: `/api/v1/positions`*

### Get Last Known Position
- **GET** `/positions/{imei}/last`
- **Description**: Retrieves the last known position for a specific IMEI device.
- **Authorization**: The device owner or a user with the `GOD` role.
- **Responses**:
    - `200 OK`: Returns a `Position` object.
