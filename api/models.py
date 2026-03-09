from django.db import models
from django.contrib.auth.models import AbstractUser

class User(AbstractUser):
    ROLE_CHOICES = [
        ('STUDENT', 'Student'),
        ('PATHOLOGIST', 'Pathologist'),
        ('ADMIN', 'Admin'),
    ]
    
    role = models.CharField(max_length=20, choices=ROLE_CHOICES, default='STUDENT')
    phone = models.CharField(max_length=20, blank=True, null=True)
    profile_image = models.URLField(blank=True, null=True)
    institution = models.CharField(max_length=255, blank=True, null=True)
    specialization = models.CharField(max_length=255, blank=True, null=True)
    date_of_birth = models.DateField(blank=True, null=True)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)


class Case(models.Model):
    STATUS_CHOICES = [
        ('PENDING', 'Pending'),
        ('ANALYZED', 'Analyzed'),
        ('VALIDATED', 'Validated'),
    ]
    
    case_id = models.CharField(max_length=50, unique=True)
    title = models.CharField(max_length=255)
    description = models.TextField(blank=True)
    image_url = models.URLField()
    medical_record_number = models.CharField(max_length=100, blank=True, null=True)
    patient_age = models.IntegerField(blank=True, null=True)
    patient_gender = models.CharField(max_length=10, blank=True, null=True)
    diagnosis = models.CharField(max_length=255, blank=True, null=True)
    is_malignant = models.BooleanField(default=False)
    confidence_score = models.FloatField(blank=True, null=True)
    status = models.CharField(max_length=20, choices=STATUS_CHOICES, default='PENDING')
    created_by = models.ForeignKey(User, on_delete=models.SET_NULL, null=True, blank=True)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)
    
    class Meta:
        db_table = 'cases'


class Message(models.Model):
    sender = models.ForeignKey(User, on_delete=models.CASCADE, related_name='sent_messages')
    recipient = models.ForeignKey(User, on_delete=models.CASCADE, related_name='received_messages')
    content = models.TextField()
    is_read = models.BooleanField(default=False)
    created_at = models.DateTimeField(auto_now_add=True)
    
    class Meta:
        db_table = 'messages'

