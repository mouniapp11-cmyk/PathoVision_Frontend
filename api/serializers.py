from rest_framework import serializers
from .models import User, Case, Message
from django.contrib.auth import authenticate

class UserDataSerializer(serializers.ModelSerializer):
    """
    Serializer that returns user data in the format expected by the Android app
    """
    id = serializers.SerializerMethodField()
    name = serializers.SerializerMethodField()
    phone_number = serializers.SerializerMethodField()
    hospital_affiliation = serializers.SerializerMethodField()
    license_id = serializers.SerializerMethodField()
    date_of_birth = serializers.SerializerMethodField()
    profile_picture = serializers.SerializerMethodField()

    class Meta:
        model = User
        fields = ['id', 'name', 'email', 'role', 'phone_number', 'hospital_affiliation', 'license_id', 'date_of_birth', 'profile_picture']
    
    def get_id(self, obj):
        return str(obj.id)  # Convert to string for app compatibility
    
    def get_name(self, obj):
        # Return full name if available, otherwise username
        if obj.first_name and obj.last_name:
            return f"{obj.first_name} {obj.last_name}"
        elif obj.first_name:
            return obj.first_name
        return obj.username
    
    def get_phone_number(self, obj):
        return obj.phone or None
    
    def get_hospital_affiliation(self, obj):
        return obj.institution or None
    
    def get_license_id(self, obj):
        return getattr(obj, 'license_id', None)
    
    def get_date_of_birth(self, obj):
        return str(obj.date_of_birth) if obj.date_of_birth else None
    
    def get_profile_picture(self, obj):
        return obj.profile_image or None


class UserRegistrationSerializer(serializers.ModelSerializer):
    password = serializers.CharField(write_only=True)
    confirm_password = serializers.CharField(write_only=True, required=False)
    name = serializers.CharField(write_only=True, required=False)
    username = serializers.CharField(required=False)

    class Meta:
        model = User
        fields = ['username', 'email', 'password', 'confirm_password', 'name', 'first_name', 'last_name', 'role', 'phone']
        extra_kwargs = {'username': {'required': False}}
    
    def validate(self, data):
        # If confirm_password is provided, validate it matches
        confirm_password = data.pop('confirm_password', None)
        if confirm_password and data['password'] != confirm_password:
            raise serializers.ValidationError({"password": "Passwords do not match"})
        
        # If 'name' is provided instead of 'username', use it
        if 'name' in data and 'username' not in data:
            data['username'] = data.pop('name')
        elif 'name' in data:
            data.pop('name')  # Remove name if username is also present
        
        # Ensure username is set (fallback to email username part if needed)
        if 'username' not in data or not data['username']:
            data['username'] = data['email'].split('@')[0]
            
        return data
    
    def create(self, validated_data):
        user = User.objects.create_user(
            username=validated_data['username'],
            email=validated_data['email'],
            password=validated_data['password'],
            first_name=validated_data.get('first_name', ''),
            last_name=validated_data.get('last_name', ''),
            role=validated_data.get('role', 'STUDENT'),
            phone=validated_data.get('phone', '')
        )
        return user


class LoginSerializer(serializers.Serializer):
    username = serializers.CharField(required=False)
    email = serializers.CharField(required=False)
    password = serializers.CharField(write_only=True)
    
    def validate(self, data):
        from .models import User
        
        # Support both username and email for login
        identifier = data.get('username') or data.get('email')
        if not identifier:
            raise serializers.ValidationError("Username or email is required")
        
        # If email is provided, look up the username
        if '@' in identifier:
            try:
                user_obj = User.objects.get(email=identifier)
                username = user_obj.username
            except User.DoesNotExist:
                raise serializers.ValidationError("Invalid credentials")
        else:
            username = identifier
        
        user = authenticate(username=username, password=data['password'])
        if not user:
            raise serializers.ValidationError("Invalid credentials")
        data['user'] = user
        return data


class CaseSerializer(serializers.ModelSerializer):
    class Meta:
        model = Case
        fields = '__all__'
        read_only_fields = ['created_at', 'updated_at']


class MessageSerializer(serializers.ModelSerializer):
    sender_username = serializers.CharField(source='sender.username', read_only=True)
    recipient_username = serializers.CharField(source='recipient.username', read_only=True)
    
    class Meta:
        model = Message
        fields = ['id', 'sender', 'sender_username', 'recipient', 'recipient_username', 'content', 'is_read', 'created_at']
        read_only_fields = ['created_at']
