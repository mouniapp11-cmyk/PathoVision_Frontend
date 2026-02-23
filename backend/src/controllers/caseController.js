const Case = require('../models/Case');
const User = require('../models/User');

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

module.exports = { createCase, getCases, getCaseById };
