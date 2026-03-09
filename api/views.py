from rest_framework import viewsets, status
from rest_framework.response import Response
from rest_framework.decorators import action
from rest_framework.permissions import IsAuthenticated, AllowAny
from rest_framework_simplejwt.tokens import RefreshToken
from .models import User, Case, Message
from .serializers import UserDataSerializer, UserRegistrationSerializer, LoginSerializer, CaseSerializer, MessageSerializer
import requests
from django.conf import settings

class UserViewSet(viewsets.ModelViewSet):
    queryset = User.objects.all()
    serializer_class = UserDataSerializer
    permission_classes = [IsAuthenticated]
    
    @action(detail=False, methods=['post'], permission_classes=[AllowAny])
    def register(self, request):
        serializer = UserRegistrationSerializer(data=request.data)
        if serializer.is_valid():
            user = serializer.save()
            refresh = RefreshToken.for_user(user)
            return Response({
                'message': 'Account created successfully',
                'token': str(refresh.access_token),
                'user': UserDataSerializer(user).data
            }, status=status.HTTP_201_CREATED)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
    
    @action(detail=False, methods=['post'], permission_classes=[AllowAny])
    def login(self, request):
        serializer = LoginSerializer(data=request.data)
        if serializer.is_valid():
            user = serializer.validated_data['user']
            refresh = RefreshToken.for_user(user)
            return Response({
                'message': 'Login successful',
                'token': str(refresh.access_token),
                'user': UserDataSerializer(user).data
            })
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
    
    @action(detail=False, methods=['get'])
    def profile(self, request):
        return Response(UserDataSerializer(request.user).data)
    
    @action(detail=False, methods=['post'])
    def update_profile(self, request):
        user = request.user
        serializer = UserRegistrationSerializer(user, data=request.data, partial=True)
        if serializer.is_valid():
            serializer.save()
            return Response(UserDataSerializer(user).data)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)


class CaseViewSet(viewsets.ModelViewSet):
    queryset = Case.objects.all().order_by('-created_at')
    serializer_class = CaseSerializer
    permission_classes = [IsAuthenticated]
    
    @action(detail=False, methods=['get'])
    def my_cases(self, request):
        cases = Case.objects.filter(created_by=request.user)
        return Response(CaseSerializer(cases, many=True).data)
    
    @action(detail=True, methods=['post'])
    def analyze(self, request, pk=None):
        case = self.get_object()
        if not case.image_url:
            return Response({"error": "No image"}, status=status.HTTP_400_BAD_REQUEST)
        try:
            ml_url = f"{settings.ML_SERVICE_URL}/predict"
            resp = requests.post(ml_url, json={"image_url": case.image_url}, timeout=30)
            if resp.status_code == 200:
                result = resp.json()
                case.confidence_score = result.get('confidence', 0)
                case.is_malignant = result.get('class_name') == 'Malignant'
                case.status = 'ANALYZED'
                case.save()
                return Response(CaseSerializer(case).data)
            return Response({"error": "ML error"}, status=500)
        except Exception as e:
            return Response({"error": str(e)}, status=500)


class MessageViewSet(viewsets.ModelViewSet):
    queryset = Message.objects.all()
    serializer_class = MessageSerializer
    permission_classes = [IsAuthenticated]
    
    def get_queryset(self):
        return Message.objects.filter(recipient=self.request.user) | Message.objects.filter(sender=self.request.user)
    
    def create(self, request, *args, **kwargs):
        request.data['sender'] = request.user.id
        return super().create(request, *args, **kwargs)
    
    @action(detail=False, methods=['get'])
    def inbox(self, request):
        msgs = Message.objects.filter(recipient=request.user).order_by('-created_at')
        return Response(MessageSerializer(msgs, many=True).data)

