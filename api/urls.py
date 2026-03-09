from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import UserViewSet, CaseViewSet, MessageViewSet

router = DefaultRouter(trailing_slash=False)
router.register(r'auth', UserViewSet, basename='user')
router.register(r'cases', CaseViewSet, basename='case')
router.register(r'messages', MessageViewSet, basename='message')

urlpatterns = [
    path('', include(router.urls)),
]
