const User = require('../models/User');
const bcrypt = require('bcrypt');
const jwt = require('jsonwebtoken');

const register = async (req, res) => {
    try {
        const { name, email, password, role } = req.body;

        // Check if user exists
        const existingUser = await User.findOne({ where: { email } });
        if (existingUser) {
            return res.status(400).json({ message: 'User already exists' });
        }

        // Hash password
        const salt = await bcrypt.genSalt(10);
        const password_hash = await bcrypt.hash(password, salt);

        // Create user
        const user = await User.create({
            name,
            email,
            password_hash,
            role,
        });

        // Generate token
        const token = jwt.sign(
            { id: user.id, role: user.role },
            process.env.JWT_SECRET,
            { expiresIn: '1d' }
        );

        res.status(201).json({
            token,
            user: {
                id: user.id,
                name: user.name,
                email: user.email,
                role: user.role,
            },
        });
    } catch (error) {
        console.error(error);
        res.status(500).json({ message: 'Server error' });
    }
};

const login = async (req, res) => {
    try {
        const { email, password } = req.body;

        // Find user
        const user = await User.findOne({ where: { email } });
        if (!user) {
            return res.status(400).json({ message: 'Invalid credentials' });
        }

        // Check password
        const isMatch = await bcrypt.compare(password, user.password_hash);
        if (!isMatch) {
            return res.status(400).json({ message: 'Invalid credentials' });
        }

        // Generate token
        const token = jwt.sign(
            { id: user.id, role: user.role },
            process.env.JWT_SECRET,
            { expiresIn: '1d' }
        );

        res.json({
            token,
            user: {
                id: user.id,
                name: user.name,
                email: user.email,
                role: user.role,
            },
        });
    } catch (error) {
        console.error(error);
        res.status(500).json({ message: 'Server error' });
    }
};

const getPatients = async (req, res) => {
    try {
        const patients = await User.findAll({
            where: { role: 'PATIENT' },
            attributes: ['id', 'name', 'email']
        });
        res.json(patients);
    } catch (error) {
        console.error(error);
        res.status(500).json({ message: 'Server error fetching patients' });
    }
};

const getDoctors = async (req, res) => {
    try {
        const doctors = await User.findAll({
            where: { role: 'PATHOLOGIST' },
            attributes: ['id', 'name', 'email', 'profile_picture']
        });
        res.json(doctors);
    } catch (error) {
        console.error(error);
        res.status(500).json({ message: 'Server error fetching doctors' });
    }
};

const getProfile = async (req, res) => {
    try {
        const userId = req.user.id;
        const user = await User.findByPk(userId, {
            attributes: ['id', 'name', 'email', 'role', 'phone_number', 'hospital_affiliation', 'license_id', 'profile_picture']
        });

        if (!user) {
            return res.status(404).json({ message: 'User not found' });
        }

        res.json(user);
    } catch (error) {
        console.error(error);
        res.status(500).json({ message: 'Server error fetching profile' });
    }
};

const updateProfile = async (req, res) => {
    try {
        const userId = req.user.id;
        const { name, phone_number, hospital_affiliation, license_id } = req.body;

        const user = await User.findByPk(userId);
        if (!user) {
            return res.status(404).json({ message: 'User not found' });
        }

        if (name) user.name = name;
        if (phone_number) user.phone_number = phone_number;
        if (hospital_affiliation) user.hospital_affiliation = hospital_affiliation;
        if (license_id) user.license_id = license_id;
        if (req.file) {
            user.profile_picture = req.file.path.replace(/\\/g, '/');
        }

        await user.save();

        res.json({
            message: 'Profile updated successfully',
            user: {
                id: user.id,
                name: user.name,
                email: user.email,
                role: user.role,
                phone_number: user.phone_number,
                hospital_affiliation: user.hospital_affiliation,
                license_id: user.license_id,
                profile_picture: user.profile_picture
            }
        });
    } catch (error) {
        console.error(error);
        res.status(500).json({ message: 'Server error updating profile' });
    }
};

const changePassword = async (req, res) => {
    try {
        const userId = req.user.id;
        const { current_password, new_password } = req.body;

        if (!current_password || !new_password) {
            return res.status(400).json({ message: 'Current and new password are required' });
        }

        const user = await User.findByPk(userId);
        if (!user) {
            return res.status(404).json({ message: 'User not found' });
        }

        const isMatch = await bcrypt.compare(current_password, user.password_hash);
        if (!isMatch) {
            return res.status(400).json({ message: 'Current password is incorrect' });
        }

        const salt = await bcrypt.genSalt(10);
        user.password_hash = await bcrypt.hash(new_password, salt);
        await user.save();

        res.json({ message: 'Password updated successfully' });
    } catch (error) {
        console.error(error);
        res.status(500).json({ message: 'Server error updating password' });
    }
};

module.exports = { register, login, getPatients, getDoctors, getProfile, updateProfile, changePassword };
