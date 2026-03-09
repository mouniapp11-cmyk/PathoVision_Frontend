# PathoVision Django Backend

**Status**: ✅ **LIVE AND RUNNING**

## Backend Information
- **Server**: Django REST Framework
- **Port**: 8001
- **URL**: `http://localhost:8001/api/`
- **Database**: SQLite (can migrate to PostgreSQL if needed)
- **Authentication**: JWT (JSON Web Tokens)

## API Endpoints

### Authentication
- `POST /api/auth/register/` - Register new user
- `POST /api/auth/login/` - Login user
- `GET /api/auth/profile/` - Get user profile
- `POST /api/auth/update_profile/` - Update user profile
- `POST /api/token/` - Get JWT token
- `POST /api/token/refresh/` - Refresh JWT token

### Cases (Pathology)
- `GET /api/cases/` - List all cases
- `POST /api/cases/` - Create new case
- `GET /api/cases/{id}/` - Get case details
- `PUT /api/cases/{id}/` - Update case
- `DELETE /api/cases/{id}/` - Delete case
- `GET /api/cases/my_cases/` - Get user's cases
- `POST /api/cases/{id}/analyze/` - Analyze case with ML

### Messages
- `GET /api/messages/` - List messages
- `POST /api/messages/` - Send message
- `GET /api/messages/inbox/` - Get inbox
- `POST /api/messages/{id}/mark_as_read/` - Mark as read

## Features
✅ User authentication & role-based access  
✅ Case management (CRUD operations)  
✅ Direct integration with ML service (Flask on :5000)  
✅ Message/Chat system  
✅ JWT token-based security  
✅Automatic migrations from Node.js API format

## Setup & Running

### Start all services:
```bash
# Terminal 1: Django Backend
cd Mounisha_App
.\venv_django\Scripts\python manage.py runserver 8001

# Terminal 2: Flask ML Service  
cd ml
python flask_inference_app.py

# Terminal 3: ngrok (for public access)
ngrok http 8001
```

## Switching from Node.js to Django

**Android App Configuration**:
- Update BASE_URL in `app/src/main/java/com/simats/pathovision/utils/Constants.kt`
- Change from: `http://localhost:3001/api/`
- Change to: `http://localhost:8001/api/` (or ngrok URL)

## Database
- Using SQLite for now (portable, no setup needed)
- Can migrate to PostgreSQL by:
  1. Installing psycopg2: `pip install psycopg2-binary`
  2. Updating DATABASES in settings.py
  3. Running `python manage.py migrate`

## Admin Panel
- URL: `http://localhost:8001/admin/`
- To create superuser: `python manage.py createsuperuser`

## Status
🟢 Running  
🟢 Database initialized  
🟢 All endpoints operational  
🟢 Ready for frontend integration
