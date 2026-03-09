const Case = require('../models/Case');
const User = require('../models/User');
const { generatePathologyReport, listGeminiModels } = require('../services/geminiService');

const createCase = async (req, res) => {
    try {
        const { tissue_type, doctor_notes, patient_id } = req.body;
        const pathologist_id = req.user.id;

        // Auto-generate case number
        const caseCount = await Case.count();
        const caseNumber = String(caseCount + 1).padStart(3, '0');
        const title = `Case #${caseNumber}`;

        // For now, we'll use a placeholder image until file upload is implemented
        const image_url = 'placeholder.jpg';

        // Mock AI Analysis (will be replaced with real analysis later)
        const ai_prediction = Math.random() > 0.5 ? 'Malignant' : 'Benign';
        const confidence_score = (Math.random() * (0.99 - 0.70) + 0.70).toFixed(2);

        const newCase = await Case.create({
            title,
            image_url,
            doctor_notes,
            pathologist_id,
            patient_id: patient_id || null,
            ai_prediction,
            confidence_score,
        });

        // Fetch the created case with associations
        const createdCase = await Case.findByPk(newCase.id, {
            include: [
                { model: User, as: 'Pathologist', attributes: ['name', 'email'] },
                { model: User, as: 'Patient', attributes: ['name', 'email'] },
            ],
        });

        res.status(201).json(createdCase);
    } catch (error) {
        console.error(error);
        res.status(500).json({ message: 'Server error' });
    }
};

const getCases = async (req, res) => {
    try {
        const { role, id } = req.user;
        let whereClause = {};

        if (role === 'PATHOLOGIST') {
            whereClause = { pathologist_id: id };
        } else if (role === 'PATIENT') {
            whereClause = { patient_id: id };
        } else if (role === 'STUDENT') {
            // Students see all cases or anonymized ones (simplified for MVP)
            whereClause = {};
        }

        const cases = await Case.findAll({
            where: whereClause,
            include: [
                { model: User, as: 'Pathologist', attributes: ['name', 'email'] },
                { model: User, as: 'Patient', attributes: ['name', 'email'] },
            ],
            order: [['created_at', 'DESC']],
        });

        res.json(cases);
    } catch (error) {
        console.error(error);
        res.status(500).json({ message: 'Server error' });
    }
};

const getCaseById = async (req, res) => {
    try {
        const { id } = req.params;
        const caseItem = await Case.findByPk(id, {
            include: [
                { model: User, as: 'Pathologist', attributes: ['name', 'email'] },
                { model: User, as: 'Patient', attributes: ['name', 'email'] },
            ],
        });

        if (!caseItem) {
            return res.status(404).json({ message: 'Case not found' });
        }

        res.json(caseItem);
    } catch (error) {
        console.error(error);
        res.status(500).json({ message: 'Server error' });
    }
};

/**
 * Generate pathology report using Gemini AI
 * Only generates if report doesn't already exist
 */
const generateReport = async (req, res) => {
    try {
        const { id } = req.params;
        const caseItem = await Case.findByPk(id, {
            include: [
                { model: User, as: 'Pathologist', attributes: ['name', 'email'] },
                { model: User, as: 'Patient', attributes: ['name', 'email', 'date_of_birth'] },
            ],
        });

        if (!caseItem) {
            return res.status(404).json({ message: 'Case not found' });
        }

        // Check if report already exists (avoid unnecessary API calls)
        if (caseItem.pathologist_report) {
            console.log(`Report already exists for case ${id}, returning cached version`);
            return res.json({
                report: JSON.parse(caseItem.pathologist_report),
                caseId: caseItem.id,
                mrn: caseItem.mrn,
                patientName: caseItem.Patient?.name || 'Anonymous',
                patientDob: caseItem.Patient?.date_of_birth 
                    ? new Date(caseItem.Patient.date_of_birth).toLocaleDateString('en-US', { year: 'numeric', month: '2-digit', day: '2-digit' })
                    : 'Not provided',
                aiPrediction: caseItem.ai_prediction,
                confidence: parseFloat(caseItem.confidence_score) * 100,
                cached: true
            });
        }

        // Prepare data for Gemini API
        const caseData = {
            patientName: caseItem.Patient?.name || 'Anonymous',
            patientAge: caseItem.Patient?.date_of_birth 
                ? new Date().getFullYear() - new Date(caseItem.Patient.date_of_birth).getFullYear() + ' years'
                : 'Not provided',
            specimenSource: caseItem.tissue_type || 'Tissue specimen',
            aiPrediction: caseItem.ai_prediction || 'Unknown',
            confidence: parseFloat(caseItem.confidence_score) * 100,
            findings: [
                caseItem.ai_prediction === 'Malignant' ? 'Invasive Carcinoma detected' : 'Benign tissue characteristics',
                'High cellular density analysis complete',
                'Morphological features analyzed'
            ],
            clinicalInfo: caseItem.doctor_notes || 'No clinical history provided.'
        };

        console.log('Generating report for case:', id);
        
        // Generate report using Gemini AI
        const report = await generatePathologyReport(caseData);

        // Update case with generated report and validation status
        await caseItem.update({
            pathologist_report: JSON.stringify(report),
            validation_status: 'validated',
            validated_at: new Date(),
            validated_by_id: req.user.id
        });

        res.json({
            report,
            caseId: caseItem.id,
            mrn: caseItem.mrn,
            patientName: caseItem.Patient?.name || 'Anonymous',
            patientDob: caseItem.Patient?.date_of_birth 
                ? new Date(caseItem.Patient.date_of_birth).toLocaleDateString('en-US', { year: 'numeric', month: '2-digit', day: '2-digit' })
                : 'Not provided',
            aiPrediction: caseItem.ai_prediction,
            confidence: parseFloat(caseItem.confidence_score) * 100,
            cached: false
        });

    } catch (error) {
        console.error('Error generating report:', error);
        res.status(500).json({ 
            message: 'Failed to generate report',
            error: error.message 
        });
    }
};

/**
 * Sign off the pathology report
 * Marks the report as finalized and ready for patient viewing
 */
const signOffReport = async (req, res) => {
    try {
        const { id } = req.params;
        const caseItem = await Case.findByPk(id);

        if (!caseItem) {
            return res.status(404).json({ message: 'Case not found' });
        }

        // Verify that report exists before signing off
        if (!caseItem.pathologist_report) {
            return res.status(400).json({ message: 'Cannot sign off report: Report not generated yet' });
        }

        // Update to signed off status
        await caseItem.update({
            validation_status: 'signed_off',
            signed_off_at: new Date()
        });

        console.log(`Report signed off for case ${id} by pathologist ${req.user.id}`);

        res.json({
            message: 'Report successfully signed off',
            validation_status: 'signed_off'
        });

    } catch (error) {
        console.error('Error signing off report:', error);
        res.status(500).json({ 
            message: 'Failed to sign off report',
            error: error.message 
        });
    }
};

const getGeminiModels = async (req, res) => {
    try {
        const models = await listGeminiModels();
        res.json({ models });
    } catch (error) {
        console.error('Error listing Gemini models:', error);
        res.status(500).json({
            message: 'Failed to list Gemini models',
            error: error.message
        });
    }
};

module.exports = { createCase, getCases, getCaseById, generateReport, signOffReport, getGeminiModels };
